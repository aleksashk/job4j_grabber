package ru.job4j.grabber.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ofPattern;

public class Post {
    private int id;
    private String name;
    private String link;
    private String description;
    private LocalDateTime created;

    public Post() {
    }

    public Post(int id, String name, String link, String description, LocalDateTime created) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.description = description;
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return id == post.id && Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, link);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = getCreated().format(formatter);
        return String.format("Вакансия: %s%nСсылка: %s%nОписание: %s%nДата создания: %s%n", getName(), getLink(), getDescription(), formattedDate);
    }
}
