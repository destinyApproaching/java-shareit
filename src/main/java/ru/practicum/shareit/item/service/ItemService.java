package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;


import java.util.List;

public interface ItemService {
    List<ItemDto> getItems();

    ItemDto createItem(ItemDto itemDto, int userId);

    ItemDto updateItem(ItemDto itemDto, int id, int userId);

    ItemDto getItemById(int id);

    List<ItemDto> search(String text);
}
