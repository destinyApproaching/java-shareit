package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;
    private final String description;
    private User requester;
    private LocalDateTime created;
}
