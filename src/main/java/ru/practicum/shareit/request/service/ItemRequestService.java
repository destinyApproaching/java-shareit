package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getOwnItemRequests(Long userId);

    List<ItemRequestDto> getItemRequests(Long userId, Pageable pageable);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}
