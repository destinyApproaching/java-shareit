package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.UserException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceImplTest {
    private final ItemService itemService;

    private final UserService userService;

    private final ItemRequestService itemRequestService;

    private ItemDto itemDto;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .name("item")
                .description("desc")
                .available(true)
                .build();
        userDto = UserDto.builder()
                .name("user")
                .email("@")
                .build();
    }

    @Test
    void createItemInvalidUserTest() {
        assertThrows(UserNotFoundException.class, () -> itemService.createItem(itemDto, 1L));
    }

    @Test
    void createItemTest() {
        userService.createUser(userDto);
        ItemDto item = itemService.createItem(itemDto, 1L);
        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("item", item.getName());
    }

    @Test
    void createRequestItemTest() {
        userService.createUser(userDto);
        userDto.setEmail("user2@email.com");
        userService.createUser(userDto);
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("desc")
                .build();
        itemRequestService.createItemRequest(2L, requestDto);
        itemDto.setRequestId(1L);
        ItemDto item = itemService.createItem(itemDto, 1L);
        assertNotNull(item);
        assertEquals(1L, item.getRequestId());
    }

    @Test
    void getItemTest() {
        createItemTest();
        ItemDto item = itemService.getItemById(1L, 1L);
        assertEquals(1L, item.getId());
        assertEquals("item", item.getName());
    }

    @Test
    void getAllItems() {
        createItemTest();
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemDto> items = itemService.getItems(1L, pageable);
        assertEquals(1, items.size());
    }

    @Test
    void searchItemTest() {
        createItemTest();
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemDto> items = itemService.search("item", pageable);
        assertEquals(1, items.size());
    }

    @Test
    void searchItemNullMatchTest() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemDto> items = itemService.search("item", pageable);
        assertNotNull(items);
    }

    @Test
    void updateItemTest() {
        createItemTest();
        itemDto.setDescription("desc");
        itemDto.setName("updatedName");
        ItemDto item = itemService.updateItem(itemDto, 1L, 1L);
        assertNotNull(item);
        assertEquals("updatedName", item.getName());
        assertEquals("desc", item.getDescription());
    }

    @Test
    void updateItemNoOwnerTest() {
        createItemTest();
        itemDto.setDescription("desc");
        itemDto.setName("updatedName");
        assertThrows(UserNotFoundException.class, () -> itemService.updateItem(itemDto, 1L, 2L));
    }

    @Test
    void updateItemNotExistsTest() {
        assertThrows(UserNotFoundException.class, () -> itemService.updateItem(itemDto, 1L, 2L));
    }

    @Test
    void addCommentNoBooking() {
         userDto = UserDto.builder()
                .name("user1")
                .email("@")
                .build();
        createItemTest();
        userDto.setEmail("@1");
        userService.createUser(userDto);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("text");
        assertThrows(UserException.class, () -> itemService.createComment(2L, 1L, commentDto));
    }

}