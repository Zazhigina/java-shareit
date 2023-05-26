package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(message = "Поле name не должно быть пустым")
    private String name;
    @NotBlank(message = "Поле description не должно быть пустым")
    @Size(max = 200)
    private String description;
    @NotNull
    private Boolean available;
}
