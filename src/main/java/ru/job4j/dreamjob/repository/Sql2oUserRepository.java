package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.model.User;

import java.util.Optional;

@Repository
public class Sql2oUserRepository implements UserRepository {

    private final Sql2o sql2o;

    public Sql2oUserRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Optional<User> save(User user) {
        try (Connection connection = sql2o.open()) {
            String sql = """
                    INSERT INTO users (email, name, password)
                    VALUES (:email, :name, :password);
                    """;
            Query query = connection.createQuery(sql, true)
                    .addParameter("email", user.getEmail())
                    .addParameter("name", user.getName())
                    .addParameter("password", user.getPassword());
            int generatedId = query.executeUpdate().getKey(Integer.class);
            user.setId(generatedId);
        } catch (Exception exception) {
            user = null;
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        try (Connection connection = sql2o.open()) {
            String sql = """
                    SELECT * FROM users WHERE email = :email and password = :password
                    """;
            Query query = connection.createQuery(sql);
            query.addParameter("email", email);
            query.addParameter("password", password);
            User user = query.executeAndFetchFirst(User.class);
            return Optional.ofNullable(user);
        }
    }
    public void deleteAllUsers() {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery("DELETE FROM users");
            query.executeUpdate();
        }
    }
}
