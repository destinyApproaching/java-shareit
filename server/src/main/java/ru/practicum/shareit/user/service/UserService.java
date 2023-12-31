package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto);

    void deleteUser(Long id);

    List<UserDto> getUsers();

    UserDto getUserById(Long id);
}
