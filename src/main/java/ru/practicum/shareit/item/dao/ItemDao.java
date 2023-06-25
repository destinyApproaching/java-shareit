package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemDao {
    List<Item> getItems();

    Item createItem(Item item);

    Item updateItem(Item item);

    Item getItemById(int id);

    List<Item> search(String text);
}
