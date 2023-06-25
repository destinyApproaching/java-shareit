package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@Builder
public class ItemRequestDto {
    private int id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
