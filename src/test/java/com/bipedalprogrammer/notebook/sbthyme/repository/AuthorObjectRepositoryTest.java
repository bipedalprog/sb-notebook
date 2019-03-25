package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.repository.verticies.AuthorObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthorObjectRepositoryTest {

    @Autowired
    private AuthorPersistence repository;


    @Before
    public void prepareRepository() {
        List<AuthorObject> priors = repository.findAllAuthors();
        priors.forEach( (a) -> repository.delete(a));
    }

    @Test
    public void saveShouldAssignId() {
        AuthorObject authorObject = repository.newAuthor("Donald", "Duck", "donald@example.com");
        authorObject.setFirstName("Donny");
        AuthorObject updated = repository.update(authorObject);
        assertNotNull(updated);
        assertThat(updated.getFirstName(), equalTo(authorObject.getFirstName()));
    }

    @Test
    public void updateShouldChangeEntity() {
        AuthorObject authorObject = repository.newAuthor("William", "Shatner", "kirk@example.com");
        authorObject.setEmailAddress("tjhooker@example.com");
        AuthorObject updated = repository.update(authorObject);
        assertEquals(authorObject.getAuthorId(), updated.getAuthorId());
    }
    @Test
    public void loadAuthorByEmail() {
        AuthorObject created = repository.newAuthor("Isaac", "Asimov", "asimov@example.com");
        assertNotNull(created);
        AuthorObject found = repository.findByEmailAddress(created.getEmailAddress());
        assertNotNull(found);
        assertThat(found.getAuthorId(), equalTo(created.getAuthorId()));
    }

    @Test
    public void loadAuthorByName() {
        AuthorObject created = repository.newAuthor("Isaac", "Asimov", "asimmov@example.com");
        AuthorObject stored = repository.update(created);
        assertNotNull(stored);
        List<AuthorObject> found = repository.findAuthorByName(stored.getFirstName(), stored.getLastName());
        assertNotNull(found);
        assertThat(found.get(0).getAuthorId(), equalTo(stored.getAuthorId()));
    }

}
