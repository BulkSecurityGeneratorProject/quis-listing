package com.manev.quislisting.service.post.dto.builder;

import com.manev.quislisting.service.post.dto.Author;

/**
 * Created by tmanev on 2/8/2017.
 */
public final class PostUserBuilder {
    private Long id;
    private String login;
    private String firstName;
    private String lastName;

    private PostUserBuilder() {
    }

    public static PostUserBuilder aPostUser() {
        return new PostUserBuilder();
    }

    public PostUserBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public PostUserBuilder withLogin(String login) {
        this.login = login;
        return this;
    }

    public PostUserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public PostUserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Author build() {
        Author author = new Author();
        author.setId(id);
        author.setLogin(login);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        return author;
    }
}
