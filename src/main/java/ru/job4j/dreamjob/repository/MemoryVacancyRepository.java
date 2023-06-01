package ru.job4j.dreamjob.repository;

import ru.job4j.dreamjob.model.Vacancy;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryVacancyRepository implements VacancyRepository {

    private static final MemoryVacancyRepository INSTANCE = new MemoryVacancyRepository();

    private int nextId = 1;

    private final Map<Integer, Vacancy> vacancies = new HashMap<>();

    private MemoryVacancyRepository() {
        save(new Vacancy(0, "Intern Java Developer", "description1", LocalDateTime.of(2023, 1, 10, 12, 30)));
        save(new Vacancy(0, "Junior Java Developer", "description2", LocalDateTime.of(2023, 2, 11, 12, 38)));
        save(new Vacancy(0, "Junior+ Java Developer", "description3", LocalDateTime.of(2023, 4, 3, 15, 30)));
        save(new Vacancy(0, "Middle Java Developer", "description4", LocalDateTime.of(2023, 5, 1, 12, 36)));
        save(new Vacancy(0, "Middle+ Java Developer", "description5", LocalDateTime.of(2023, 5, 10, 10, 10)));
        save(new Vacancy(0, "Senior Java Developer", "description6", LocalDateTime.of(2023, 5, 22, 17, 20)));
    }

    public static MemoryVacancyRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        vacancy.setId(nextId++);
        vacancies.put(vacancy.getId(), vacancy);
        return vacancy;
    }

    @Override
    public boolean deleteById(int id) {
        return vacancies.remove(id) != null;
    }

    @Override
    public boolean update(Vacancy vacancy) {
        return vacancies.computeIfPresent(vacancy.getId(), (id, oldVacancy) ->
                new Vacancy(oldVacancy.getId(), vacancy.getTitle(), vacancy.getDescription(), vacancy.getCreationDate())) != null;
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        return Optional.ofNullable(vacancies.get(id));
    }

    @Override
    public Collection<Vacancy> findAll() {
        return vacancies.values();
    }
}
