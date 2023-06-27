package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class IndexControllerTest {

    @Test
    public void whenRequestIndexPageThenGetIndexPage() {
        IndexController indexController = new IndexController();
        assertThat(indexController.getIndex()).isEqualTo("index");
    }

}