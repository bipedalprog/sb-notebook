package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.NotebookObject;
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
public class NotebookObjectRepositoryTest {
    private static final String NOTEBOOK_NAME = "Test NotebookObject";

    @Autowired
    private NotebookPersistence repository;

    @Before
    public void before() {
        List<NotebookObject> prior = repository.getNotebooks();
        for (NotebookObject notebookObject : prior) repository.delete(notebookObject);
    }

    @Test
    public void shouldCreateNotebook() {
        NotebookObject notebookObject = repository.create(NOTEBOOK_NAME, "/tmp/notebookObject");
        assertNotNull(notebookObject);
        assertThat(notebookObject.getTitle(), equalTo(NOTEBOOK_NAME));
    }

    @Test
    public void shouldNotDuplicateTitle() {
        NotebookObject notebookObject = repository.create(NOTEBOOK_NAME, "/tmp/notebookObject");
        try {
            NotebookObject ohno = repository.create(NOTEBOOK_NAME, "/tmp/notebookObject");
            fail("Duplicate title should throw exception.");
        } catch (NotebookExistsException e) {
            assertTrue("Yay!", true);
        }
    }
}
