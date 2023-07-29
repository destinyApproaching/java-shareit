package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(Long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long userId, ItemDto itemDto) {
        return patch("/" + itemDto.getId(), userId, itemDto);
    }

    public ResponseEntity<Object> getItem(Long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByUserId(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> searchText(Long userId, String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size,
                "text", text
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> createComment(Long userId, Long itemId, CommentDto commentDto) {
        return post(String.format("/%s/comment", itemId), userId, commentDto);
    }

//    private void validateItemDto(ItemDto itemDto, boolean isUpdate) {
//        if (isUpdate && (itemDto.getName() != null && itemDto.getName().isBlank()) ||
//                (!isUpdate && (itemDto.getName() == null || itemDto.getName().isBlank()))) {
//            throw new ValidationException("Не указано поле Name");
//        }
//
//        if (isUpdate && (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) ||
//                !isUpdate && (itemDto.getDescription() == null || itemDto.getDescription().isBlank())) {
//            throw new ValidationException("Не указано поле Description");
//        }
//
//        if (!isUpdate && itemDto.getAvailable() == null) {
//            throw new ValidationException("Отсуствует поле Available");
//        }
//
//    }
}
