package ru.job4j.grabber.store;

import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private final Connection cnn;

    public PsqlStore(Properties properties) {
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
            this.cnn = DriverManager.getConnection(
                    properties.getProperty("jdbc.url"),
                    properties.getProperty("jdbc.username"),
                    properties.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void save(Post post) {
        String script = "insert into post(name, description, link, created) values(?, ?, ?, ?) on conflict (link) do nothing";
        try (PreparedStatement preparedStatement = cnn.prepareStatement(script)) {
            preparedStatement.setString(1, post.getName());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving post", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        String script = "select * from post";
        try (PreparedStatement preparedStatement = cnn.prepareStatement(script)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(
                            new Post(
                                    resultSet.getInt("id"),
                                    resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getString("link"),
                                    resultSet.getTimestamp("created").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting posts", e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        String script = "select * from post where id = ?";
        try (PreparedStatement preparedStatement = cnn.prepareStatement(script)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Post(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getString("link"),
                            resultSet.getTimestamp("created").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding post by id", e);
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("jdbc.driver", "org.postgresql.Driver");
        properties.setProperty("jdbc.url", "jdbc:postgresql://localhost:5432/psql_store");
        properties.setProperty("jdbc.username", "postgres");
        properties.setProperty("jdbc.password", "password");

        PsqlStore psqlStore = new PsqlStore(properties);

        Post post1 = new Post(1, "Post#1", "This is a post#1", "http://link.to/post1", LocalDateTime.now());
        Post post2 = new Post(2, "Post#2", "This is a post#2", "http://link.to/post2", LocalDateTime.now());
        psqlStore.save(post1);
        psqlStore.save(post2);

        System.out.println("All posts: ");
        for (Post post : psqlStore.getAll()) {
            System.out.println(post + " id = " + post.getId());
        }


        int targetId = 1;
        Post postById = psqlStore.findById(targetId);
        if (postById != null) {
            System.out.println("Post was find by id: " + targetId + " is: ");
            System.out.println(postById);
        } else {
            System.out.println("Post with id: " + targetId + " didn't find.");
        }

        try {
            psqlStore.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}