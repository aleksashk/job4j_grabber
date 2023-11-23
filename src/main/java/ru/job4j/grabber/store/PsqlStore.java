package ru.job4j.grabber.store;

import ru.job4j.grabber.model.Post;

import java.sql.*;
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
                            getPost(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting posts", e);
        }
        return posts;
    }

    private static Post getPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    @Override
    public Post findById(int id) {
        String script = "select * from post where id = ?";
        try (PreparedStatement preparedStatement = cnn.prepareStatement(script)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return getPost(resultSet);
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
}