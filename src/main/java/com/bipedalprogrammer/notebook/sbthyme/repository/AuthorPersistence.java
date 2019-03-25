package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.AuthorObject;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


import static com.bipedalprogrammer.notebook.sbthyme.repository.OrientStore.*;

@Component
public class AuthorPersistence extends Persistor {
    private static final String FIND_BY_EMAIL_ADDRESS = "SELECT FROM Authors WHERE email = ?";
    private static final String FIND_BY_AUTHOR_ID = "SELECT FROM Authors WHERE authorId = ?";
    private static final String FIND_BY_NAME = "SELECT FROM Authors WHERE firstName = ? AND lastName = ?";
    private static final String FIND_ALL = "SELECT FROM Authors";

    private Logger logger = LoggerFactory.getLogger(AuthorPersistence.class);

    @Autowired
    public AuthorPersistence(OrientStore orientStore) {
        super(orientStore);
    }

    public AuthorObject newAuthor(String firstName, String lastName, String emailAddress) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = createAuthor(db, firstName, lastName, emailAddress);
            AuthorObject authorObject = new AuthorObject(firstName, lastName, emailAddress);
            authorObject.setAuthorId(vertex.getProperty(AUTHOR_ID));
            return authorObject;
        } catch (Exception ex) {
            logger.info("Cannot create author.", ex);
        }
        return null;
    }

    public AuthorObject update(AuthorObject authorObject) {

        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadAuthor(db, authorObject.getAuthorId());
            vertex.setProperty(AUTHOR_FIRST_NAME, authorObject.getFirstName());
            vertex.setProperty(AUTHOR_LAST_NAME, authorObject.getLastName());
            vertex.setProperty(AUTHOR_EMAIL, authorObject.getEmailAddress());
            db.save(vertex);
        } catch (Exception ex) {
            logger.info("Unable to save authorObject.", ex);
        }

        return authorObject;

    }

    public AuthorObject findByEmailAddress(String emailAddress) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OResultSet resultSet = db.query(FIND_BY_EMAIL_ADDRESS, emailAddress);
            AuthorObject authorObject = new AuthorObject();
            if (resultSet.hasNext()) {
                OResult result = resultSet.next();
                result.getVertex().ifPresent(v -> {
                    authorFromVertex(authorObject, v);
                });
            }
            resultSet.close();
            return authorObject;
        } catch (Exception ex) {
            logger.info("Unable to save author.", ex);
        }

        return null;
    }

    public List<AuthorObject> findAuthorByName(String firstName, String lastName) {
        List<AuthorObject> authorObjects = new ArrayList<>();
        try (ODatabaseSession db = orientStore.getSession()) {
            try (OResultSet rs = db.query(FIND_BY_NAME, firstName, lastName)) {
                while (rs.hasNext()) {
                    rs.next().getVertex().ifPresent(v -> {
                        AuthorObject authorObject = new AuthorObject();
                        authorFromVertex(authorObject, v);
                        authorObjects.add(authorObject);
                    });
                }
            }
        }
        return authorObjects;
    }

    public List<AuthorObject> findAllAuthors() {
        List<AuthorObject> authorObjects = new ArrayList<AuthorObject>();
        try (ODatabaseSession db = orientStore.getSession()) {
            for (ODocument doc : db.browseClass(AUTHOR_SCHEMA)) {
                doc.asVertex().ifPresent(v -> {
                    AuthorObject authorObject = new AuthorObject();
                    authorFromVertex(authorObject, v);
                    authorObjects.add(authorObject);
                });
            }
        }
        return authorObjects;
    }

    public boolean delete(AuthorObject authorObject) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadAuthor(db, authorObject.getAuthorId());
            db.delete(vertex);
        }
        return true;
    }


}
