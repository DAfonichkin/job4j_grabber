package ru.job4j.grabber;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore() {
        init();
    }

    public PsqlStore(Connection cn) {
        this.cnn = cn;
    }

    private void init() {
        try (InputStream is = new FileInputStream("db/liquibase.properties")) {
            Properties config = new Properties();
            config.load(is);
            Class.forName(config.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    private Post convertResultToPost(ResultSet rsl) throws SQLException {
        return new Post(
                rsl.getInt("id"),
                rsl.getString("link"),
                rsl.getString("name"),
                rsl.getString("text"),
                rsl.getTimestamp("created").toLocalDateTime());
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(
                "insert into post (link,name,text,created)"
                        + "values (?,?,?,?)"
                        + "ON CONFLICT (link) "
                        + "DO NOTHING", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getLink());
            statement.setString(2, post.getTitle());
            statement.setString(3, post.getDescription());
            Timestamp timestamp = Timestamp.valueOf(post.getCreated());
            statement.setTimestamp(4, timestamp);
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                while (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("select * from post")) {
            try (ResultSet rslSet = statement.executeQuery()) {
                while (rslSet.next()) {
                    posts.add(convertResultToPost(rslSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rslSet = statement.executeQuery()) {
                while (rslSet.next()) {
                    post = convertResultToPost(rslSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }
}
