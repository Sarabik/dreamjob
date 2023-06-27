package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.CandidateService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class CandidateControllerTest {
    private CandidateService candidateService;

    private CityService cityService;

    private CandidateController candidateController;

    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithcandidates() {
        Candidate candidate1 = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        Candidate candidate2 = new Candidate(2, "test2", "desc2", now(), false, 3, 4);
        List<Candidate> expectedcandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedcandidates);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getAll(model);
        Object actualcandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualcandidates).isEqualTo(expectedcandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationPage(model);
        var actualcandidates = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualcandidates).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectTocandidatesPage() throws Exception {
        Candidate candidate = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.create(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);

    }


    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        RuntimeException expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.create(new Candidate(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenGetExistingCandidateById() {
        Candidate candidate = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        when(candidateService.findById(anyInt())).thenReturn(Optional.of(candidate));

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getById(model, candidate.getId());
        Object actualCandidate = model.getAttribute("candidate");

        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(view).isEqualTo("candidates/one");
    }

    @Test
    public void whenGetNotExistingCandidateById() {
        var expectedException = new RuntimeException("Кандидат с указанным идентификатором не найден");
        when(candidateService.findById(anyInt())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getById(model, 1);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
        assertThat(view).isEqualTo("errors/404");
    }

    @Test
    public void whenCandidateSuccessfullyUpdated() throws IOException {
        Candidate candidate = new Candidate(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenCandidateIsNotUpdated() {
        RuntimeException expectedException = new RuntimeException("Кандидат с указанным идентификатором не найден");
        when(candidateService.update(any(), any())).thenThrow(expectedException);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(new Candidate(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenSuccessfullyDeleteCandidate() {
        when(candidateService.deleteById(anyInt())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.delete(model, 1);
        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenCandidateIsNotDeleted() {
        RuntimeException expectedException = new RuntimeException("Кандидат с указанным идентификатором не найден");
        when(candidateService.deleteById(anyInt())).thenReturn(false);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.delete(model, 1);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }
}