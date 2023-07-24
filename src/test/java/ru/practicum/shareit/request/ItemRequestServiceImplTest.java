package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.UserNotFoundException;
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
public class ItemRequestServiceImplTest {
    private final ItemRequestService itemRequestService;

    private final UserService userService;

    private final ItemService itemService;

    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUpItemRequestDto() {
        itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .build();
    }

    @Test
    void createRequestNoUserTest() {
        assertThrows(UserNotFoundException.class, () -> itemRequestService.createItemRequest(itemRequestDto, 1L));
    }

    @Test
    void createRequestTest() {
        userService.createUser(UserDto.builder()
                .name("name")
                .email("@")
                .build());
        itemRequestDto.setDescription("description");
        ItemRequestDto createdItemRequestDto = itemRequestService.createItemRequest(itemRequestDto, 1L);
        assertNotNull(createdItemRequestDto);
        assertEquals(1L, createdItemRequestDto.getId());
        assertEquals("description", createdItemRequestDto.getDescription());
        assertNotNull(createdItemRequestDto.getCreated());
    }

    @Test
    void getOwnerRequestsTest() {
        createRequestTest();
        List<ItemRequestDto> items = itemRequestService.getOwnItemRequests(1L);
        assertEquals(1, items.size());
        userService.createUser(UserDto.builder()
                .name("name1")
                .email("@1")
                .build());
        items = itemRequestService.getOwnItemRequests(2L);

        assertEquals(0, items.size());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.getOwnItemRequests(3L));

    }

    @Test
    void getAllRequestsTest() {
        userService.createUser(UserDto.builder()
                .name("name")
                .email("@")
                .build());

        for (int i = 0; i < 20; i++) {
            itemRequestDto.setDescription("description" + i);
            itemRequestService.createItemRequest(itemRequestDto, 1L);
        }

        userService.createUser(UserDto.builder()
                .name("name1")
                .email("@1")
                .build());

        Pageable pageable = PageRequest.of(0 / 11, 11);

        List<ItemRequestDto> items = itemRequestService.getItemRequests(2L, pageable);
        assertEquals(11, items.size());

        pageable = PageRequest.of(1 / 10, 10);

        items = itemRequestService.getItemRequests(2L, pageable);
        assertEquals(10, items.size());

        pageable = PageRequest.of(2 / 10, 10);

        items = itemRequestService.getItemRequests(2L, pageable);
        assertEquals(10, items.size());

    }

    @Test
    void getRequestByIdTest() {
        createRequestTest();
        ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(1L, 1L);
        assertNotNull(itemRequestDto);
        assertEquals(1, itemRequestDto.getId());
        assertEquals("description", itemRequestDto.getDescription());
    }

    @Test
    void getRequestByIdItemListTest() {
        createRequestTest();
        userService.createUser(UserDto.builder()
                .name("name1")
                .email("@1")
                .build());

        ItemDto itemDto = ItemDto.builder()
                .name("item")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();
        itemService.createItem(itemDto, 1L);

        ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(1L, 1L);
        assertEquals(1, itemRequestDto.getItems().size());
        assertEquals("item", itemRequestDto.getItems().get(0).getName());
    }
}