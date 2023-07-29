package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().equals("")) {
            throw new ItemRequestException("Отсутствует описание запрашиваемого предмета.");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(userOptional.get());
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getOwnItemRequests(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            List<ItemDto> itemDtos =  itemRepository.findAllByRequestId(itemRequestDto.getId()).stream()
                    .map(ItemMapper::toItemRequestDto)
                    .collect(Collectors.toList());
            itemRequestDto.setItems(itemDtos);
        }
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestDto> getItemRequests(Long userId, Pageable pageable) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findAllByRequesterIdNot(userId, pageable).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            List<ItemDto> itemDtos =  itemRepository.findAllByRequestId(itemRequestDto.getId()).stream()
                    .map(ItemMapper::toItemRequestDto)
                    .collect(Collectors.toList());
            itemRequestDto.setItems(itemDtos);
        }
        return itemRequestDtos;
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }
        Optional<ItemRequest> itemRequestOptional = itemRequestRepository.findById(requestId);
        if (itemRequestOptional.isEmpty()) {
            throw new UserNotFoundException(String.format("Запрос с id = %d не существует.", requestId));
        }
        List<ItemDto> itemDtos = itemRepository.findItemsByRequestId(requestId).stream()
                .map(ItemMapper::toItemRequestDto)
                .collect(Collectors.toList());
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequestOptional.get());
        itemRequestDto.setItems(itemDtos);
        return itemRequestDto;
    }
}
