package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.EmailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class InMemoryUserDao implements UserDao {
    private Long id = 1L;

    private final List<User> users = new ArrayList<>();

    @Override
    public User createUser(User user) {
        user.setId(getId());
        emailChecker(user);
        users.add(user);
        increment();
        return user;
    }

    @Override
    public User updateUser(User user) {
        int index = -1;
        for (User iUser : users) {
            if (Objects.equals(iUser.getId(), user.getId())) {
                index = users.indexOf(iUser);
                break;
            }
        }
        if (index != -1) {
            if (user.getName() == null || user.getName().isEmpty()) {
                user.setName(users.get(index).getName());
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                user.setEmail(users.get(index).getEmail());
            } else {
                for (User iUser : users) {
                    if (iUser.getEmail().equals(user.getEmail()) && !Objects.equals(iUser.getId(), user.getId())) {
                        throw new DuplicateEmailException(String.format("Пользователь с email = %s уже существует.",
                                user.getEmail()));
                    }
                }
            }
            users.set(index, user);
            return user;
        }  else {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", user.getId()));
        }
    }

    @Override
    public void deleteUser(int id) {
        int index;
        for (User user : users) {
            if (user.getId() == id) {
                index = users.indexOf(user);
                users.remove(index);
                return;
            }
        }
        throw new UserNotFoundException(String.format("Пользователь с id = %d не найден.", id));
    }

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public User getUserById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        throw new UserNotFoundException(String.format("Пользователь с id = %d не найден.", id));
    }

    private Long getId() {
        return id;
    }

    private void increment() {
        id++;
    }

    private void emailChecker(User user) {
        for (User iUser : users) {
            if (iUser.getEmail().equals(user.getEmail())) {
                throw new DuplicateEmailException(String.format("Пользователь с email = %s уже существует.",
                        user.getEmail()));
            }
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new EmailException("Отсутствует email.");
        }
        if (!user.getEmail().contains("@")) {
            throw new EmailException("Неверно указан email.");
        }
    }
}