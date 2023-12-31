package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
    private long itemId;
    private long authorId;
    private String authorName;
    private LocalDateTime created;
}