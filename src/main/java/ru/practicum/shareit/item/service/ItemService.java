package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(Long id);

    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long id, Long userId);

    ItemDto getItemById(Long id, Long userId);

    List<ItemDto> search(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
