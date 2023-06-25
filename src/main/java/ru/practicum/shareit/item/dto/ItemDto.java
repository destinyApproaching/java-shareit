package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemDto {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("available")
    private Boolean available;
    @JsonProperty("owner")
    private int owner;
    @JsonProperty("request")
    private ItemRequest request;
}