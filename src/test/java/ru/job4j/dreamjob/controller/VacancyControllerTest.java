package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class VacancyControllerTest {
    private VacancyService vacancyService;

    private CityService cityService;

    private VacancyController vacancyController;

    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        vacancyService = mock(VacancyService.class);
        cityService = mock(CityService.class);
        vacancyController = new VacancyController(vacancyService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestVacancyListPageThenGetPageWithVacancies() {
        Vacancy vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        Vacancy vacancy2 = new Vacancy(2, "test2", "desc2", now(), false, 3, 4);
        List<Vacancy> expectedVacancies = List.of(vacancy1, vacancy2);
        when(vacancyService.findAll()).thenReturn(expectedVacancies);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getAll(model);
        Object actualVacancies = model.getAttribute("vacancies");

        assertThat(view).isEqualTo("vacancies/list");
        assertThat(actualVacancies).isEqualTo(expectedVacancies);
    }

    @Test
    public void whenRequestVacancyCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = vacancyController.getCreationPage(model);
        var actualVacancies = model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/create");
        assertThat(actualVacancies).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {
        Vacancy vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.save(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(vacancy);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.create(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);

    }


    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        RuntimeException expectedException = new RuntimeException("Failed to write file");
        when(vacancyService.save(any(), any())).thenThrow(expectedException);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.create(new Vacancy(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenGetExistingVacancyById() {
        Vacancy vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        when(vacancyService.findById(anyInt())).thenReturn(Optional.of(vacancy));

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getById(model, vacancy.getId());
        Object actualVacancy = model.getAttribute("vacancy");

        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(view).isEqualTo("vacancies/one");
    }

    @Test
    public void whenGetNotExistingVacancyById() {
        var expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");
        when(vacancyService.findById(anyInt())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getById(model, 1);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
        assertThat(view).isEqualTo("errors/404");
    }

    @Test
    public void whenVacancySuccessfullyUpdated() throws IOException {
        Vacancy vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.update(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.update(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenVacancyIsNotUpdated() {
        RuntimeException expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");
        when(vacancyService.update(any(), any())).thenThrow(expectedException);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.update(new Vacancy(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenSuccessfullyDeleteVacancy() {
        when(vacancyService.deleteById(anyInt())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.delete(model, 1);
        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenVacancyIsNotDeleted() {
        RuntimeException expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");
        when(vacancyService.deleteById(anyInt())).thenReturn(false);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.delete(model, 1);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }
}