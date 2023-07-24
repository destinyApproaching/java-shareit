package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private UserDto userDto;

    @BeforeEach
    void setUpUserDto() {
        userDto = UserDto.builder()
                .id(1L)
                .name("user")
                .email("user@yandex.ru")
                .build();
    }

    @Test
    void createUserTest() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(any()))
                .thenReturn(userDto);

        mockMvc.perform(get("/users" + "/" + 1))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsersEmptyTest() throws Exception {
        when(userService.getUsers()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users"))
                .andExpect(jsonPath("$", hasSize(0)));

    }

    @Test
    void getAllUsersTwoUserTest() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(userDto, userDto));

        mockMvc.perform(get("/users"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void updateUserTest() throws Exception {
        when(userService.updateUser(any()))
                .thenReturn(userDto);

        mockMvc.perform(patch("/users" + "/" + 1)
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class));
    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users" + "/" + 1))
                .andExpect(status().isOk());
    }
}