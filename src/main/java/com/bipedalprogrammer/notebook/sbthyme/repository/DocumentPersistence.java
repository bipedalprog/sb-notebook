package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.Document;
import com.orientechnologies.orient.core.db.ODatabaseSession;
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

    public Document newDocument(Document document) {
        try (ODatabaseSession db = orientStore.getSession()) {
            Long documentId = db.getMetadata().getSequenceLibrary().getSequence(DOCUMENT_SEQUENCE).next();
            OVertex vertex = db.newVertex(DOCUMENT_SCHEMA);
            vertex.setProperty(DOCUMENT_ID, documentId);
            setVertexProperties(vertex, document);
            db.save(vertex);
            document.setDocumentId(documentId);
            Set<OVertex> authors = resolveAuthors(db, vertex, document.getAuthors());

            return document;
        }
    }

    public Document save(Document document) {
        try (ODatabaseSession db = orientStore.getSession()) {
            AtomicReference<OVertex> vertexRef = new AtomicReference<>();
            try (OResultSet rs = db.query(FIND_DOCUMENTS_BY_ID, document.getDocumentId())) {
                if (rs.hasNext()) {
                    rs.next().getVertex().ifPresent(v -> {
                        setVertexProperties(v, document);
                        vertexRef.set(db.save(v));
                    });
                } else {
                    logger.warn("Document id " + document.getDocumentId() + "not found. Save failed.");
                    return null;
                }
            }
            // TODO See if we have added any authors.

            Set<OVertex> authors = resolveAuthors(db, vertexRef.get(), document.getAuthors());
            return document;
        }
    }

    public List<Document> findAllDocuments() {

        try (ODatabaseSession db = orientStore.getSession()) {
            List<Document> documents = new ArrayList<>();
            for (ODocument doc : db.browseClass("DOCUMENT_SCHEMA")) {
                doc.asVertex().ifPresent(v -> {
                    documents.add(documentFromVertex(v));
                });
            }
            return documents;
        }
    }
}
