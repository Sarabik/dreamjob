package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.model.Vacancy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oVacancyRepositoryTest {

    private static Sql2oVacancyRepository sql2oVacancyRepository;

    private static Sql2oFileRepository sql2oFileRepository;

    private static File file;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oVacancyRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);

        sql2oVacancyRepository = new Sql2oVacancyRepository(sql2o);
        sql2oFileRepository = new Sql2oFileRepository(sql2o);

        // нужно сохранить хотя бы один файл, т.к. Vacancy от него зависит
        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearVacancies() {
        var vacancies = sql2oVacancyRepository.findAll();
        for (var vacancy : vacancies) {
            sql2oVacancyRepository.deleteById(vacancy.getId());
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = sql2oVacancyRepository.save(new Vacancy(0, "title", "description", creationDate, true, 1, file.getId()));
        Vacancy savedVacancy = sql2oVacancyRepository.findById(vacancy.getId()).get();
        assertThat(savedVacancy).usingRecursiveComparison().isEqualTo(vacancy);
    }

    @Test
    public void whenSaveSeveralThenGetAll() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy1 = sql2oVacancyRepository.save(new Vacancy(0, "title1", "description1", creationDate, true, 1, file.getId()));
        Vacancy vacancy2 = sql2oVacancyRepository.save(new Vacancy(0, "title2", "description2", creationDate, false, 1, file.getId()));
        Vacancy vacancy3 = sql2oVacancyRepository.save(new Vacancy(0, "title3", "description3", creationDate, true, 1, file.getId()));
        Collection<Vacancy> result = sql2oVacancyRepository.findAll();
        assertThat(result).isEqualTo(List.of(vacancy1, vacancy2, vacancy3));
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        assertThat(sql2oVacancyRepository.findAll()).isEqualTo(emptyList());
        assertThat(sql2oVacancyRepository.findById(0)).isEqualTo(empty());
    }

    @Test
    public void whenDeleteThenGetEmptyOptional() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = sql2oVacancyRepository.save(new Vacancy(0, "title", "description", creationDate, true, 1, file.getId()));
        boolean isDeleted = sql2oVacancyRepository.deleteById(vacancy.getId());
        Optional<Vacancy> savedVacancy = sql2oVacancyRepository.findById(vacancy.getId());
        assertThat(isDeleted).isTrue();
        assertThat(savedVacancy).isEqualTo(empty());
    }

    @Test
    public void whenDeleteByInvalidIdThenGetFalse() {
        assertThat(sql2oVacancyRepository.deleteById(0)).isFalse();
    }

    @Test
    public void whenUpdateThenGetUpdated() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = sql2oVacancyRepository.save(new Vacancy(0, "title", "description", creationDate, true, 1, file.getId()));
        Vacancy updatedVacancy = new Vacancy(
                vacancy.getId(), "new title", "new description", creationDate.plusDays(1),
                !vacancy.getVisible(), 1, file.getId()
        );
        boolean isUpdated = sql2oVacancyRepository.update(updatedVacancy);
        Vacancy savedVacancy = sql2oVacancyRepository.findById(updatedVacancy.getId()).get();
        assertThat(isUpdated).isTrue();
        assertThat(savedVacancy).usingRecursiveComparison().isEqualTo(updatedVacancy);
    }

    @Test
    public void whenUpdateUnExistingVacancyThenGetFalse() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = new Vacancy(0, "title", "description", creationDate, true, 1, file.getId());
        boolean isUpdated = sql2oVacancyRepository.update(vacancy);
        assertThat(isUpdated).isFalse();
    }

}