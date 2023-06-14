package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oUserRepositoryTest {
    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);

        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @BeforeEach
    public void clearDb() {
        sql2oUserRepository.deleteAllUsers();
    }

    @Test
    public void whenSaveUserThenFindIt() {
        User addedUser = sql2oUserRepository.save(new User("email_1@mail.ru", "Aleksandr", "123")).get();
        User foundUser = sql2oUserRepository.findByEmailAndPassword("email_1@mail.ru", "123").get();
        assertThat(foundUser).usingRecursiveComparison().isEqualTo(addedUser);
    }

    @Test
    public void whenTryToAddSameUser() {
        User addedUser1 = sql2oUserRepository.save(new User("email_1@mail.ru", "Aleksandr", "123")).get();
        Optional<User> addedUser2 = sql2oUserRepository.save(new User("email_1@mail.ru", "Aleksandr", "123"));
        User foundUser = sql2oUserRepository.findByEmailAndPassword("email_1@mail.ru", "123").get();
        assertThat(foundUser).usingRecursiveComparison().isEqualTo(addedUser1);
        assertThat(addedUser2).isEmpty();
    }

    @Test
    public void whenUserNotFound() {
        Optional<User> user = sql2oUserRepository.findByEmailAndPassword("email_1@mail.ru", "123");
        assertThat(user).isEmpty();
    }
}