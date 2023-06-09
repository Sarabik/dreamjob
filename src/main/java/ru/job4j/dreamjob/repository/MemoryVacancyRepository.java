package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Vacancy;

import javax.annotation.concurrent.ThreadSafe;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@ThreadSafe
public class MemoryVacancyRepository implements VacancyRepository {

    private final AtomicInteger nextId = new AtomicInteger(1);

    private final Map<Integer, Vacancy> vacancies = new ConcurrentHashMap<>();

    private MemoryVacancyRepository() {
        save(new Vacancy(0, "Intern Java Developer", "description1", LocalDateTime.of(2023, 1, 10, 12, 30), true));
        save(new Vacancy(0, "Junior Java Developer", "description2", LocalDateTime.of(2023, 2, 11, 12, 38), true));
        save(new Vacancy(0, "Junior+ Java Developer", "description3", LocalDateTime.of(2023, 4, 3, 15, 30), true));
        save(new Vacancy(0, "Middle Java Developer", "description4", LocalDateTime.of(2023, 5, 1, 12, 36), true));
        save(new Vacancy(0, "Middle+ Java Developer", "description5", LocalDateTime.of(2023, 5, 10, 10, 10), true));
        save(new Vacancy(0, "Senior Java Developer", "description6", LocalDateTime.of(2023, 5, 22, 17, 20), true));
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        vacancy.setId(nextId.incrementAndGet());
        vacancies.putIfAbsent(vacancy.getId(), vacancy);
        return vacancy;
    }

    @Override
    public boolean deleteById(int id) {
        return vacancies.remove(id) != null;
    }

    @Override
    public boolean update(Vacancy vacancy) {
        return vacancies.computeIfPresent(vacancy.getId(), (id, oldVacancy) ->
                new Vacancy(oldVacancy.getId(), vacancy.getTitle(), vacancy.getDescription(),
                        vacancy.getCreationDate(), vacancy.getVisible())) != null;
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
