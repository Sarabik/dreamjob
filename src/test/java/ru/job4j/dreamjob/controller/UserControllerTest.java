package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;

    private  UserController userController;

    private HttpServletRequest request;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        request = new MockHttpServletRequest();
    }

    @Test
    public void whenRequestRegistrationPageThenGetIt() {
        assertThat(userController.getRegistrationPage()).isEqualTo("users/register");
    }

    @Test
    public void whenSuccessfullyRegisterUser() {
        User user = new User("mail@mail.com", "name", "password");
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.register(model, user);
        User actualUser = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    public void whenUserIsNotRegistered() {
        RuntimeException expectedException = new RuntimeException("Пользователь с такой почтой уже существует");
        when(userService.save(any())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.register(model, new User());
        Object actualExceptionMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/register");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestLoginPageThenGetIt() {
        assertThat(userController.getLoginPage()).isEqualTo("users/login");
    }

    @Test
    public void whenUserSuccessfullyLogin() {
        User user = new User("mail@mail.com", "name", "password");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.of(user));

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.loginUser(user, model, request);
        var actualUser = request.getSession().getAttribute("user");

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    public void whenUserDidNotLoginThenErrorMessage() {
        RuntimeException expectedException = new RuntimeException("Почта или пароль введены неверно");
        when(userService.findByEmailAndPassword(any(), any())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = userController.loginUser(new User(), model, request);
        Object actualExceptionMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenUserLogout() {
        User user = new User("mail@mail.com", "name", "password");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.of(user));
        ConcurrentModel model = new ConcurrentModel();
        userController.loginUser(user, model, request);
        var userWhenLogin = request.getSession().getAttribute("user");

        String view = userController.logout(request.getSession());
        var userAfterLogout = request.getSession().getAttribute("user");

        assertThat(view).isEqualTo("redirect:/users/login");
        assertThat(userWhenLogin).isNotNull();
        assertThat(userAfterLogout).isNull();
    }
}