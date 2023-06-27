package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileControllerTest {

    private FileService fileService;

    private FileController fileController;

    @BeforeEach
    public void initServices() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
    }

    @Test
    public void whenFindImageByIdThenGetStatus200() {
        Optional<FileDto> optionalFileDto = Optional.of(new FileDto("name.jpg", new byte[] {1, 2, 3}));
        when(fileService.getFileById(anyInt())).thenReturn(optionalFileDto);

        ResponseEntity<?> responseEntity = fileController.getById(1);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void whenDidNotFindImageByIdThenGetStatus404() {
        when(fileService.getFileById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<?> responseEntity = fileController.getById(1);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(404);
    }
}