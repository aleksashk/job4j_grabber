package ru.job4j.grabber.extractor;

import ru.job4j.grabber.model.Post;

import java.util.List;

public interface Parse {
    List<Post> list(String link);
}