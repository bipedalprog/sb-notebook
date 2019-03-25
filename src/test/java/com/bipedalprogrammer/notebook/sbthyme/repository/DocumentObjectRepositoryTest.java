package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.AuthorObject;
import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.DocumentObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.bipedalprogrammer.notebook.sbthyme.repository.verticies.AuthorObject.AUTHOR_DEFAULT_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocumentObjectRepositoryTest {
    @Autowired
    private DocumentPersistence repository;

    @Test
    public void saveShouldAssignId() {
        Set<AuthorObject> authorObjects = new HashSet<>();
        authorObjects.add(new AuthorObject("Sample", "AuthorObject", "sample@example.com"));
        DocumentObject documentObject = new DocumentObject();
        documentObject.setTitle("A Test DocumentObject");
        documentObject.setAuthors(authorObjects);
        documentObject.setRevision("1.0");
        documentObject.setRevisionDate(new Date());
        documentObject.setBody("We're all bozos on this bus.");
        DocumentObject updated = repository.newDocument(documentObject);
        assertNotNull(updated);
        assertNotNull(documentObject.getAuthors());
        assertThat(documentObject.getAuthors(), hasSize(1));
        assertThat(documentObject.getAuthors().iterator().next().getAuthorId(), not(equalTo(AUTHOR_DEFAULT_ID)));
    }

    @Test
    public void saveShouldRecognizeNewAuthors() {
        // Set up.
        Set<AuthorObject> initialAuthorObjects = new HashSet<>();
        initialAuthorObjects.add(new AuthorObject("Initial", "AuthorObject", "sample@example.com"));
        DocumentObject documentObject = new DocumentObject();
        documentObject.setTitle("Additional Authors Test");
        documentObject.setAuthors(initialAuthorObjects);
        documentObject.setRevision("1.0");
        documentObject.setRevisionDate(new Date());
        documentObject.setBody("This is the initial list of authors.");
        DocumentObject updated = repository.newDocument(documentObject);
        assertNotNull(updated);

        // Mutate.
        updated.getAuthors().add(new AuthorObject("Updated", "AuthorObject", "updated@example.com"));
        DocumentObject mutated = repository.save(updated);
        assertNotNull(mutated);
        assertTrue(mutated.getAuthors().stream()
                .anyMatch(a -> {return a.getEmailAddress().compareTo("updated@example.com") == 0;}));
        assertTrue(mutated.getAuthors().stream().allMatch(a -> {return a.getAuthorId() != AUTHOR_DEFAULT_ID;}));
    }
}
