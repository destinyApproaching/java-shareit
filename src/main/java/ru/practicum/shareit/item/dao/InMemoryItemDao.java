package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class InMemoryItemDao implements ItemDao {
    private Long id = 1L;

    private final List<Item> items = new ArrayList<>();

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public Item createItem(Item item) {
        item.setId(getId());
        validate(item);
        items.add(item);
        increment();
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        int index = -1;
        for (Item iItem : items) {
            if (Objects.equals(iItem.getId(), item.getId())) {
                index = items.indexOf(iItem);
                break;
            }
        }
        if (index != -1) {
            if (item.getName() == null || item.getName().isEmpty()) {
                item.setName(items.get(index).getName());
            }
            if (item.getDescription() == null || item.getDescription().isEmpty()) {
                item.setDescription(items.get(index).getDescription());
            }
            if (item.getAvailable() == null) {
                item.setAvailable(items.get(index).getAvailable());
            }
            items.set(index, item);
            return item;
        } else {
            throw new ItemNotFoundException(String.format("Вещь с id = %d не существует.", item.getId()));
        }
    }

    @Override
    public Item getItemById(int id) {
        for (Item item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new ItemNotFoundException(String.format("Вещь с id = %d не существует.", id));
    }

    @Override
    public List<Item> search(String text) {
        List<Item> searchItems = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return searchItems;
        }
        for (Item item : items) {
            if (item.getAvailable()) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                    searchItems.add(item);
                } else if (item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    searchItems.add(item);
                }
            }
        }
        return searchItems;
    }

    private Long getId() {
        return id;
    }

    private void increment() {
        id++;
    }

    private void validate(Item item) {
        if (item.getName() == null || item.getName().isEmpty()) {
            throw new ValidationException("Отсутствует название вещи.");
        }
        if (item.getDescription() == null || item.getDescription().isEmpty()) {
            throw new ValidationException("Отсутствует описание вещи.");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Отсутствует обозначение наличия или отсутсвия вещи.");
        }
    }
}
