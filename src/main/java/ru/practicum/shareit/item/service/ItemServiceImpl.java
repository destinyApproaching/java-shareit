package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(ItemDao itemDao, UserService userService) {
        this.itemDao = itemDao;
        this.userService = userService;
    }

    @Override
    public List<ItemDto> getItems() {
        return itemDao.getItems().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, int userId) {
        itemDto.setId(0);
        itemDto.setOwner(userId);
        Item item = ItemMapper.toItem(itemDto);
        if (userValidator(userId)) {
            return ItemMapper.toItemDto(itemDao.createItem(item));
        } else {
            throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", userId));
        }
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, int id, int userId) {
        itemDto.setId(id);
        for (Item item : itemDao.getItems()) {
            if (item.getId() == id && item.getOwner() == userId) {
                itemDto.setOwner(item.getOwner());
            }
        }
        Item item = ItemMapper.toItem(itemDto);
        if (item.getOwner() == userId) {
            if (userValidator(userId)) {
                return ItemMapper.toItemDto(itemDao.updateItem(item));
            }
        }
        throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", userId));
    }

    @Override
    public ItemDto getItemById(int id) {
        return ItemMapper.toItemDto(itemDao.getItemById(id));
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemDao.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private boolean userValidator(int userId) {
        for (UserDto user : userService.getUsers()) {
            if (user.getId() == userId) {
                return true;
            }
        }
        return false;
    }
}
