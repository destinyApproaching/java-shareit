package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserDao {
    User createUser(User user);

    User updateUser(User user);

    void deleteUser(int id);

    List<User> getUsers();

    User getUserById(int id);

}
