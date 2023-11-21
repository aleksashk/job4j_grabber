package ru.job4j.grabber.store;

import ru.job4j.grabber.model.Post;

import java.util.List;

public class MemStore implements Store {
    @Override
    public void save(Post post) {

    }

    @Override
    public List<Post> getAll() {
        return null;
    }

    @Override
    public Post findById(int id) {
        return null;
    }
}
