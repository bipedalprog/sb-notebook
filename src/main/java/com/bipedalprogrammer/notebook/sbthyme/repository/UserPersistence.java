package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.UserObject;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.bipedalprogrammer.notebook.sbthyme.repository.OrientStore.*;

@Component
public class UserPersistence {

    private OrientStore orientStore;
    private PasswordEncoder encoder;

    private Logger logger = LoggerFactory.getLogger(UserPersistence.class);

    private static final String FIND_USERS_BY_NAME = "SELECT FROM Users WHERE email = ?";
    private static final String FIND_ALL_USERS = "SELECT FROM Users";

    @Autowired
    public UserPersistence(OrientStore orientStore, PasswordEncoder encoder) {
        this.encoder = encoder;
        this.orientStore = orientStore;
    }

    public boolean addUser(String username, String password) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex user = db.newVertex(USER_SCHEMA);
            user.setProperty(USER_EMAIL, username);
            user.setProperty(USER_PASSWORD, encoder.encode(password));
            user.setProperty(OrientStore.USER_ENABLED, true);
            user.setProperty(OrientStore.USER_ROLES, UserObject.ROLE_USER);
            OVertex saved = db.save(user);
            logger.info("UserObject saved at RID: {}", saved.getRecord().getIdentity().toStream());
        } catch (ORecordDuplicatedException dup) {
            logger.info("UserObject {} already exists.", username);
            return false;
        } catch (Exception e) {
            logger.error("addUser failed.", e);
            return false;
        }
        return true;
    }

    public UserObject findByUsername(String username) {
        UserObject userObject = new UserObject();
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadUser(db, username);
            if (vertex == null) {
                logger.info("UserObject {} was not found.", username);
            }
            userObject = userFromVertex(vertex);
        } catch (Exception ex) {
            logger.info("Unable to load userObject.", ex);
        }
        return userObject;
    }

    public List<UserObject> findAll() {
        List<UserObject> userObjects = new ArrayList<>();
        try (ODatabaseSession db = orientStore.getSession()) {
            try (OResultSet rs = db.query(FIND_ALL_USERS)) {
                if (rs.hasNext()) {
                    OResult result = rs.next();
                    result.getVertex().ifPresent(v -> {
                        userObjects.add(userFromVertex(v));
                    });
                }
            }
        } catch (Exception ex) {
            logger.info("Unable to find userObjects.", ex);
        }
        return userObjects;
    }

    public boolean delete(UserObject userObject) {
        boolean success = true;
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadUser(db, userObject.getUsername());
            if (vertex != null) db.delete(vertex);
        } catch (Exception ex) {
            logger.info("Delete failed.", ex);
            success = false;
        }
        return success;
    }

    private UserObject userFromVertex(OVertex v) {
        UserObject u = new UserObject();
        u.setUsername(v.getProperty(USER_EMAIL));
        u.setPassword(v.getProperty(USER_PASSWORD));
        u.setEnabled(v.getProperty(USER_ENABLED));
        String roles = v.getProperty(USER_ROLES);
        String[] roleNames = roles.split(",");
        for (String name : roleNames) {
            u.grant(name);
        }
        return u;
    }

    private OVertex loadUser(ODatabaseSession db, String username) {
        AtomicReference<OVertex> user = new AtomicReference<>();
        try (OResultSet rs = db.query(FIND_USERS_BY_NAME, username)) {
            if (rs.hasNext()) {
                OResult result = rs.next();
                result.getVertex().ifPresent(v -> {
                    user.set(v);
                });
            }
        }
        return user.get();
    }

}
