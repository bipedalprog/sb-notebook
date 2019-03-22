package com.bipedalprogrammer.notebook.sbthyme.repository;

public class NotebookExistsException extends RuntimeException {
    private String title;

    public NotebookExistsException(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return String.format("The notebook titled %s already exists.", title);
    }
}
