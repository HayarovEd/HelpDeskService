package ru.impuls.helpdeskservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupEditDto {

    @NotBlank(message = "Название группы обязательно")
    @Size(min = 1, max = 100, message = "Название группы должно быть от 1 до 100 символов")
    String name;
}
