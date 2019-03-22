package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.Notebook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotebookRepositoryTest {
    private static final String NOTEBOOK_NAME = "Test Notebook";

    @Autowired
    private NotebookPersistence repository;

    @Before
    public void before() {
        List<Notebook> prior = repository.getNotebooks();
        for (Notebook notebook : prior) repository.delete(notebook);
    }

    @Test
    public void shouldCreateNotebook() {
        Notebook notebook = repository.create(NOTEBOOK_NAME, "/tmp/notebook");
        assertNotNull(notebook);
        assertThat(notebook.getTitle(), equalTo(NOTEBOOK_NAME));
    }

    @Test
    public void shouldNotDuplicateTitle() {
        Notebook notebook = repository.create(NOTEBOOK_NAME, "/tmp/notebook");
        try {
            Notebook ohno = repository.create(NOTEBOOK_NAME, "/tmp/notebook");
            fail("Duplicate title should throw exception.");
        } catch (NotebookExistsException e) {
            assertTrue("Yay!", true);
        }
    }
}
