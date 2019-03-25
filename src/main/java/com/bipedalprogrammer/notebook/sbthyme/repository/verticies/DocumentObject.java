package com.bipedalprogrammer.notebook.sbthyme.repository.verticies;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DocumentObject {
    private long documentId = -1L;
    private String title;
    private String revision;
    private Date revisionDate;
    private String body;
    private Set<AuthorObject> authorObjects = new HashSet<>();

    public DocumentObject() {}

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Set<AuthorObject> getAuthors() {
        return authorObjects;
    }

    public void setAuthors(Set<AuthorObject> authorObjects) {
        this.authorObjects = authorObjects;
    }
}
