package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.CommentException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;

    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .build();
        commentDto = CommentDto.builder()
                .id(1L)
                .itemId(1L)
                .text("text")
                .build();
    }

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(any(), any())).thenReturn(itemDto);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId().intValue())));
    }

    @Test
    void createItemNoNameTest() throws Exception {
        itemDto.setName(null);
        when(itemService.createItem(any(), any()))
                .thenThrow(new ValidationException("Отсутствует название вещи."));
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemInvalidUserTest() throws Exception {
        when(itemService.createItem(any(), any()))
                .thenThrow(new UserNotFoundException(String.format("Пользователя с id = %d не существует.", 1L)));
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.updateItem(any(), any(), any())).thenReturn(itemDto);
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId().intValue())));
    }

    @Test
    void updateItemNotExists() throws Exception {
        when(itemService.updateItem(any(), any(), any()))
                .thenThrow(new ItemNotFoundException(String.format("Вещи с id = %d не существует", 1L)));
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemByIdTest() throws Exception {
        when(itemService.getItemById(any(), any())).thenReturn(itemDto);
        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId().intValue())));
    }

    @Test
    void getItemByIdNotFoundTest() throws Exception {
        when(itemService.getItemById(any(), any()))
                .thenThrow(new ItemNotFoundException(String.format("Вещи с id = %d не существует", 1L)));
        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllItemsTest() throws Exception {
        when(itemService.getItems(any(), any())).thenReturn(List.of(itemDto, itemDto));
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAllItemsInvalidUserTest() throws Exception {
        when(itemService.getItems(any(), any()))
                .thenThrow(new UserNotFoundException(String.format("Пользователя с id = %d не существует.", 1L)));
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItemTest() throws Exception {
        when(itemService.search(any(), any())).thenReturn(List.of(itemDto));
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "1")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void addCommentTest() throws Exception {
        when(itemService.createComment(any(), any(), any())).thenReturn(commentDto);
        mockMvc.perform(post("/items/{id}/comment", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId().intValue())));
    }

    @Test
    void addCommentNoTextTest() throws Exception {
        commentDto.setText(null);
        when(itemService.createComment(any(), any(), any()))
                .thenThrow(new CommentException("Текс не может быть пустым."));
        mockMvc.perform(post("/items/{id}/comment", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}
