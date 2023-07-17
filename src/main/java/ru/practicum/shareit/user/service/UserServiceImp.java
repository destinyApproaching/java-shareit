package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        userEmailChecker(user);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            User oldUser = userOptional.get();
            user.setId(oldUser.getId());
            if (user.getName() == null || user.getName().equals("")) {
                user.setName(oldUser.getName());
            }
            if (user.getEmail() == null || user.getEmail().equals("")) {
                user.setEmail(oldUser.getEmail());
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", user.getId()));
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", id));
        }
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            return UserMapper.toUserDto(userOptional.get());
        } else {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", id));
        }
    }

    private void userEmailChecker(User user) {

        if (user.getEmail() == null || user.getEmail().equals("")) {
            throw new EmailException("Отсутствует email.");
        }
        if (!user.getEmail().contains("@")) {
            throw new EmailException("Неверно указан email.");
        }
    }
}
