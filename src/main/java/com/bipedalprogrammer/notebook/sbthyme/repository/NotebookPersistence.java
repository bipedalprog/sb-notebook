package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.NotebookObject;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.bipedalprogrammer.notebook.sbthyme.repository.OrientStore.*;

@Component
public class NotebookPersistence extends Persistor {
    private Logger logger = LoggerFactory.getLogger(NotebookPersistence.class);

    private static final String FIND_NOTEBOOK_BY_TITLE = "SELECT FROM Notebooks WHERE title = ?";
    private static final String FIND_NOTEBOOK_BY_ID = "SELECT FROM Notebooks WHERE notebookId = ?";
    private static final String FIND_ALL_NOTEBOOKS = "SELECT FROM Notebooks";

    @Autowired
    public NotebookPersistence(OrientStore store) {
        super(store);
    }

    public NotebookObject create(String title, String basePath) {
        try (ODatabaseSession db = orientStore.getSession()) {
            if (doesNotebookExist(db, title)) {
                throw new NotebookExistsException(title);
            }
            db.begin();
            Date createDate = new Date();
            Long notebookId = db.getMetadata().getSequenceLibrary().getSequence(NOTEBOOK_SEQUENCE).next();
            OVertex vertex = db.newVertex(NOTEBOOK_SCHEMA);
            vertex.setProperty(NOTEBOOK_ID, notebookId);
            vertex.setProperty(NOTEBOOK_TITLE, title);
            vertex.setProperty(NOTEBOOK_BASEPATH, basePath);
            vertex.setProperty(NOTEBOOK_CREATED, createDate);
            vertex.setProperty(NOTEBOOK_UPDATED, createDate);
            OVertex saved = db.save(vertex);

            db.commit();
            return notebookFromVertex(saved);

        } catch (NotebookExistsException nbe) {
            throw nbe;
        } catch (Exception e) {
            logger.warn("Create notebook failed.", e);
        }
        return null;
    }

    public List<NotebookObject> getNotebooks() {
        List<NotebookObject> notebookObjects = new ArrayList<>();
        try (ODatabaseSession db = orientStore.getSession()) {
            try (OResultSet resultSet = db.query(FIND_ALL_NOTEBOOKS)) {
                while (resultSet.hasNext()) {
                    Optional<OVertex> next = resultSet.next().getVertex();
                    next.ifPresent(v -> {
                        notebookObjects.add(notebookFromVertex(v));
                    });
                }
            }
        }
        return notebookObjects;
    }

    public boolean delete(NotebookObject notebookObject) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadNotebook(db, notebookObject.getNotebookId());
            if (vertex != null) {
                db.delete(vertex);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.warn("Delete notebookObject failed.");
            return false;
        }
    }

    private boolean doesNotebookExist(ODatabaseSession db, String title) {
        try (OResultSet result = db.query(FIND_NOTEBOOK_BY_TITLE, title)) {
            return result.hasNext();
        }
    }

    private NotebookObject notebookFromVertex(OVertex vertex) {
        NotebookObject notebookObject = new NotebookObject();
        notebookObject.setNotebookId(vertex.getProperty(NOTEBOOK_ID).toString());
        notebookObject.setTitle(vertex.getProperty(NOTEBOOK_TITLE));
        notebookObject.setBasePath(vertex.getProperty(NOTEBOOK_BASEPATH));
        notebookObject.setCreated(vertex.getProperty(NOTEBOOK_CREATED));
        notebookObject.setUpdated(vertex.getProperty(NOTEBOOK_UPDATED));
        return notebookObject;
    }

    private OVertex loadNotebook(ODatabaseSession db, String notebookId) {
        AtomicReference<OVertex> vertexRef = new AtomicReference<>();
        try (OResultSet result = db.query(FIND_NOTEBOOK_BY_ID, notebookId)) {
            if (result.hasNext()) {
                result.next().getVertex().ifPresent(v -> {
                    vertexRef.set(v);
                });
            }
        } catch (Exception e) {
            logger.warn("Cannot load notebook.");
        }
        return vertexRef.get();
    }
}
