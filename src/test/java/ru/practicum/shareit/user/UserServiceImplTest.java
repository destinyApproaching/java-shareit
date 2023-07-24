package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.EmailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceImplTest {
    private final UserService userService;

    UserDto userDto = UserDto.builder()
            .name("name")
            .email("e@mail.com")
            .build();

    @Test
    void getUsersEmptyTest() {
        List<UserDto> users = userService.getUsers();
        assertEquals(0, users.size());
    }

    @Test
    void createUserTest() {
        UserDto createdUser = userService.createUser(userDto);
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());

        UserDto getUser = userService.getUserById(createdUser.getId());
        assertNotNull(getUser);
        assertEquals(userDto.getName(), getUser.getName());
        assertEquals(userDto.getEmail(), getUser.getEmail());
    }

    @Test
    void createUserInvalidEmailTest() {
        UserDto userInvalidEmail = UserDto.builder()
                .name("name")
                .email("mail")
                .build();

        assertThrows(EmailException.class, () -> userService.createUser(userInvalidEmail));
    }

    @Test
    void createUserEmptyNameTest() {
        UserDto userEmptyName = UserDto.builder()
                .email("e@mail.com")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(userEmptyName));
    }

    @Test
    void getUserNotExists() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(-99L));
    }

    @Test
    void getAllUsersTwoTest() {
        userService.createUser(userDto);
        userDto.setEmail("e1@yandex.ru");
        userService.createUser(userDto);
        List<UserDto> users = userService.getUsers();
        assertEquals(2, users.size());
        assertEquals("e@mail.com", users.get(0).getEmail());
        assertEquals("e1@yandex.ru", users.get(1).getEmail());
    }

    @Test
    void updateUserTest() {
        userDto = userService.createUser(userDto);
        userDto.setEmail("e1@mail.com");
        userDto.setName(null);
        UserDto updatedUser = userService.updateUser(userDto);

        assertEquals("e1@mail.com", updatedUser.getEmail());
        assertEquals("name", updatedUser.getName());

        userDto.setEmail(null);
        userDto.setName("updatedName");
        updatedUser = userService.updateUser(userDto);

        assertEquals("e1@mail.com", updatedUser.getEmail());
        assertEquals("updatedName", updatedUser.getName());
    }

    @Test
    void updateUserNotExists() {
        userDto.setId(-99L);
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userDto));
    }

    @Test
    void deleteUser() {
        userService.createUser(userDto);
        List<UserDto> users = userService.getUsers();
        assertEquals(1, users.size());
        userService.deleteUser(1L);
        users = userService.getUsers();
        assertEquals(0, users.size());
    }

    @Test
    void deleteNotExistsUser() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(-99L));
    }

}
