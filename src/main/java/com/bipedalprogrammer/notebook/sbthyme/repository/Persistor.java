package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.AuthorObject;
import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.DocumentObject;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.StreamSupport;

import static com.bipedalprogrammer.notebook.sbthyme.repository.verticies.AuthorObject.AUTHOR_DEFAULT_ID;
import static com.bipedalprogrammer.notebook.sbthyme.repository.OrientStore.*;

public class Persistor {
    protected  OrientStore orientStore;

    private static final String FIND_AUTHOR_BY_ID = "SELECT FROM Authors where authorId = ?";
    private Logger logger = LoggerFactory.getLogger(Persistor.class);

    public Persistor(OrientStore orientStore) {
        this.orientStore = orientStore;
    }

    protected OVertex createAuthor(ODatabaseSession db, String firstName, String lastName, String emailAddress) {
        Long authorId = db.getMetadata().getSequenceLibrary().getSequence(AUTHOR_SEQUENCE).next();
        OVertex vertex = db.newVertex(AUTHOR_SCHEMA);
        vertex.setProperty(AUTHOR_ID, authorId);
        vertex.setProperty(AUTHOR_FIRST_NAME, firstName);
        vertex.setProperty(AUTHOR_LAST_NAME, lastName);
        vertex.setProperty(AUTHOR_EMAIL, emailAddress);
        OVertex saved = db.save(vertex);
        return saved;
    }

    protected void authorFromVertex(AuthorObject authorObject, OVertex v) {
        authorObject.setAuthorId(v.getProperty(AUTHOR_ID));
        authorObject.setFirstName(v.getProperty(AUTHOR_FIRST_NAME));
        authorObject.setLastName(v.getProperty(AUTHOR_LAST_NAME));
        authorObject.setEmailAddress(v.getProperty(AUTHOR_EMAIL));
    }

    protected OVertex loadAuthor(ODatabaseSession db, long authorId) {
        try (OResultSet vertices = db.query(FIND_AUTHOR_BY_ID, authorId)){
            Optional<OVertex> result = vertices.next().getVertex();
            if (result.isPresent()) return result.get();
            else return null;
        }
    }

    protected Set<OVertex> getVerticiesByEmailAddress(Set<String> emails) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OResultSet resultSet = db.query("SELECT FROM Authors WHERE email in ?", emails);
            Set<OVertex> found = new HashSet<>();
            while (resultSet.hasNext()) {
                OResult result = resultSet.next();
                result.getVertex().ifPresent(v -> {
                    found.add(v);
                });
            }
            resultSet.close();
            return found;
        }
    }

    protected void setVertexProperties(OVertex v, DocumentObject documentObject) {
        v.setProperty(DOCUMENT_TITLE, documentObject.getTitle());
        v.setProperty(DOCUMENT_VERSION, documentObject.getRevision());
        v.setProperty(DOCUMENT_REVISION_DATE, documentObject.getRevisionDate());
        v.setProperty(DOCUMENT_BODY, documentObject.getBody());
    }

    protected DocumentObject documentFromVertex(OVertex v) {
        DocumentObject d = new DocumentObject();
        d.setDocumentId(v.getProperty(DOCUMENT_ID));
        d.setTitle(v.getProperty(DOCUMENT_TITLE));
        d.setRevision(v.getProperty(DOCUMENT_VERSION));
        d.setRevisionDate(v.getProperty(DOCUMENT_REVISION_DATE));
        d.setBody(v.getProperty(DOCUMENT_BODY));
        return d;
    }

    protected Set<OVertex> resolveAuthors(ODatabaseSession db, OVertex document, Set<AuthorObject> authorObjects) {
        Set<OVertex> vertices = new HashSet<>();
        Iterable<OVertex> existing = document.getVertices(ODirection.OUT, DOCUMENT_AUTHOR_SCHEMA);
        for (AuthorObject authorObject : authorObjects) {
            if (authorObject.getAuthorId() != AUTHOR_DEFAULT_ID) {
                OVertex vertex = loadAuthor(db, authorObject.getAuthorId());
                if (vertex != null) {
                    vertices.add(vertex);
                } else {
                    logger.warn("DocumentObject contained an invalid aothorId [" + authorObject.getAuthorId() + "].");
                }
                if (!documentAuthorEdgeExists(existing, vertex)) {
                    addDocumentAuthor(db, document, vertex);
                }
            } else {
                OVertex vertex = createAuthor(db, authorObject.getFirstName(), authorObject.getLastName(), authorObject.getEmailAddress());
                if (vertex != null) {
                    vertices.add(vertex);
                    authorObject.setAuthorId(vertex.getProperty(AUTHOR_ID));
                } else {
                    logger.warn("Cannot add authorObject to store.");
                }
                addDocumentAuthor(db, document, vertex);
            }
        }
        return  vertices;
    }

    protected boolean documentAuthorEdgeExists(Iterable<OVertex> existing, OVertex author) {
        return StreamSupport.stream(existing.spliterator(), true)
                .anyMatch(v -> v.getProperty(AUTHOR_ID) == author.getProperty(AUTHOR_ID));
    }

    protected OEdge addDocumentAuthor(ODatabaseSession db, OVertex document, OVertex author) {
        OEdge documentAuthor = db.newEdge(document, author, DOCUMENT_AUTHOR_SCHEMA);
        if (documentAuthor == null) {
            logger.error("Unable to create edge from document " + document.getProperty(DOCUMENT_ID)
                    + "to author " + author.getProperty(AUTHOR_ID) + ".");
        }
        ODocument saved = db.save(documentAuthor);
        if (saved == null) {
            logger.error("Edge was not saved to database.");
        }
        if (saved.asEdge().isPresent()) return saved.asEdge().get();
        else return null;

    }

    protected void loadDocumentAuthors(ODatabaseSession db, OVertex from, DocumentObject documentObject) {
        Iterable<OVertex> existing = from.getVertices(ODirection.OUT, DOCUMENT_AUTHOR_SCHEMA);
        existing.forEach( v -> {
            AuthorObject authorObject = new AuthorObject();
            authorFromVertex(authorObject, v);
            documentObject.getAuthors().add(authorObject);
        });
    }
}
