package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long id) {
        return itemService.getItems(id);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(id, userId);
    }

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long id) {
        return itemService.createItem(itemDto, id);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@Valid @RequestBody ItemDto itemDto,
                              @PathVariable Long id,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        itemDto.setId(id);
        return itemService.updateItem(itemDto, id, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(defaultValue = "") String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody CommentDto commentDto,
                                 @PathVariable Long itemId) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}
