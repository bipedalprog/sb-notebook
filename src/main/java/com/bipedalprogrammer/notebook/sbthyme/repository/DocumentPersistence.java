package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.DocumentObject;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.bipedalprogrammer.notebook.sbthyme.repository.OrientStore.*;

@Component
public class DocumentPersistence extends Persistor {
    private Logger logger = LoggerFactory.getLogger(DocumentPersistence.class);

    private static final String FIND_DOCUMENTS_BY_ID = "SELECT FROM Documents WHERE documentId = ?";

    @Autowired
    public DocumentPersistence(OrientStore orientStore) {
        super(orientStore);
    }

    public DocumentObject newDocument(DocumentObject documentObject) {
        try (ODatabaseSession db = orientStore.getSession()) {
            Long documentId = db.getMetadata().getSequenceLibrary().getSequence(DOCUMENT_SEQUENCE).next();
            OVertex vertex = db.newVertex(DOCUMENT_SCHEMA);
            vertex.setProperty(DOCUMENT_ID, documentId);
            setVertexProperties(vertex, documentObject);
            db.save(vertex);
            documentObject.setDocumentId(documentId);
            Set<OVertex> authors = resolveAuthors(db, vertex, documentObject.getAuthors());

            return documentObject;
        }
    }

    public DocumentObject save(DocumentObject documentObject) {
        try (ODatabaseSession db = orientStore.getSession()) {
            AtomicReference<OVertex> vertexRef = new AtomicReference<>();
            try (OResultSet rs = db.query(FIND_DOCUMENTS_BY_ID, documentObject.getDocumentId())) {
                if (rs.hasNext()) {
                    rs.next().getVertex().ifPresent(v -> {
                        setVertexProperties(v, documentObject);
                        vertexRef.set(db.save(v));
                    });
                } else {
                    logger.warn("DocumentObject id " + documentObject.getDocumentId() + "not found. Save failed.");
                    return null;
                }
            }
            // TODO See if we have added any authors.

            Set<OVertex> authors = resolveAuthors(db, vertexRef.get(), documentObject.getAuthors());
            return documentObject;
        }
    }

    public List<DocumentObject> findAllDocuments() {

        try (ODatabaseSession db = orientStore.getSession()) {
            List<DocumentObject> documentObjects = new ArrayList<>();
            for (ODocument doc : db.browseClass("DOCUMENT_SCHEMA")) {
                doc.asVertex().ifPresent(v -> {
                    documentObjects.add(documentFromVertex(v));
                });
            }
            return documentObjects;
        }
    }

    public boolean deleteAllDocuments() {
        try (ODatabaseSession db = orientStore.getSession()) {
            for (ORecord doc : db.browseClass(DOCUMENT_SCHEMA)) {
                db.delete(doc);
            }
        } catch (Exception e) {
            logger.warn("Unable to delete documents.");
            return false;
        }
        return true;
    }
}
