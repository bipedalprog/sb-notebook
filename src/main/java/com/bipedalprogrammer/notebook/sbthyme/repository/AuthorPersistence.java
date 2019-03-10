package com.bipedalprogrammer.notebook.sbthyme.repository;

import com.bipedalprogrammer.notebook.sbthyme.model.Author;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


import static com.bipedalprogrammer.notebook.sbthyme.repository.OrientStore.*;

@Component
public class AuthorPersistence extends Persistor {
    private static final String FIND_BY_EMAIL_ADDRESS = "SELECT FROM Authors WHERE email = ?";
    private static final String FIND_BY_AUTHOR_ID = "SELECT FROM Authors WHERE authorId = ?";
    private static final String FIND_BY_NAME = "SELECT FROM Authors WHERE firstName = ? AND lastName = ?";
    private static final String FIND_ALL = "SELECT FROM Authors";

    private Logger logger = LoggerFactory.getLogger(AuthorPersistence.class);

    @Autowired
    public AuthorPersistence(OrientStore orientStore) {
        super.orientStore = orientStore;
    }

    public Author newAuthor(String firstName, String lastName, String emailAddress) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = createAuthor(db, firstName, lastName, emailAddress);
            Author author = new Author(firstName, lastName, emailAddress);
            author.setAuthorId(vertex.getProperty(AUTHOR_ID));
            return author;
        } catch (Exception ex) {
            logger.info("Cannot create author.", ex);
        }
        return null;
    }

    public Author update(Author author) {

        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadAuthor(db, author.getAuthorId());
            vertex.setProperty(AUTHOR_FIRST_NAME, author.getFirstName());
            vertex.setProperty(AUTHOR_LAST_NAME, author.getLastName());
            vertex.setProperty(AUTHOR_EMAIL, author.getEmailAddress());
            db.save(vertex);
        } catch (Exception ex) {
            logger.info("Unable to save author.", ex);
        }

        return author;

    }

    public Author findByEmailAddress(String emailAddress) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OResultSet resultSet = db.query(FIND_BY_EMAIL_ADDRESS, emailAddress);
            Author author = new Author();
            if (resultSet.hasNext()) {
                OResult result = resultSet.next();
                result.getVertex().ifPresent(v -> {
                    authorFromVertex(author, v);
                });
            }
            resultSet.close();
            return author;
        } catch (Exception ex) {
            logger.info("Unable to save author.", ex);
        }

        return null;
    }

    public List<Author> findAuthorByName(String firstName, String lastName) {
        List<Author> authors = new ArrayList<>();
        try (ODatabaseSession db = orientStore.getSession()) {
            try (OResultSet rs = db.query(FIND_BY_NAME, firstName, lastName)) {
                while (rs.hasNext()) {
                    rs.next().getVertex().ifPresent(v -> {
                        Author author = new Author();
                        authorFromVertex(author, v);
                        authors.add(author);
                    });
                }
            }
        }
        return authors;
    }

    public List<Author> findAllAuthors() {
        List<Author> authors = new ArrayList<Author>();
        try (ODatabaseSession db = orientStore.getSession()) {
            for (ODocument doc : db.browseClass(AUTHOR_SCHEMA)) {
                doc.asVertex().ifPresent(v -> {
                    Author author = new Author();
                    authorFromVertex(author, v);
                    authors.add(author);
                });
            }
        }
        return authors;
    }

    public boolean delete(Author author) {
        try (ODatabaseSession db = orientStore.getSession()) {
            OVertex vertex = loadAuthor(db, author.getAuthorId());
            db.delete(vertex);
        }
        return true;
    }


}
