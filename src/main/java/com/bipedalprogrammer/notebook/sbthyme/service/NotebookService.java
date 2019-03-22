package com.bipedalprogrammer.notebook.sbthyme.service;

import com.bipedalprogrammer.notebook.sbthyme.repository.FileStoreConfiguration;
import com.bipedalprogrammer.notebook.sbthyme.repository.NotebookPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotebookService {
    private FileStoreConfiguration config;
    private NotebookPersistence persistor;

    @Autowired
    public NotebookService(FileStoreConfiguration config, NotebookPersistence persistor) {
        this.config = config;
        this.persistor = persistor;
    }
}
