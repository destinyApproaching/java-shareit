package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.exception.ValidateException;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    @NotBlank(groups = ValidateException.OnCreate.class)
    private String name;
    @JsonProperty("email")
    @Email(groups = {ValidateException.OnUpdate.class, ValidateException.OnCreate.class})
    @NotBlank(groups = ValidateException.OnCreate.class)
    private String email;
}
