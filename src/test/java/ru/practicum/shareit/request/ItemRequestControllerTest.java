package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    private  ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUpItemRequestDto() {
        itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .build();
    }

    @Test
    void createRequestTest() throws Exception {
        when(itemRequestService.createItemRequest(any(), any())).thenReturn(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("description", is(itemRequestDto.getDescription())));
    }

    @Test
    void getOwnItemRequestsNotExistsUserTest() throws Exception {
        when(itemRequestService.getOwnItemRequests(any()))
                .thenThrow(new UserNotFoundException(String.format("Пользователя с id = %d не существует.", 1L)));
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnItemRequestsTest() throws Exception {
        when(itemRequestService.getOwnItemRequests(any())).thenReturn(List.of(itemRequestDto, itemRequestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAllRequestTest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getItemRequestByIdNotFoundTest() throws Exception {
        when(itemRequestService.getItemRequestById(any(), any()))
                .thenThrow(new UserNotFoundException(String.format("Запрос с id = %d не существует.", 1)));
        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemRequestByIdTest() throws Exception {
        itemRequestDto.setId(1L);
        when(itemRequestService.getItemRequestById(any(), any())).thenReturn(itemRequestDto);
        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }
}