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
public class SubgroupEditDto {

    @NotBlank(message = "Название подгруппы обязательно")
    @Size(min = 2, max = 100, message = "Название подгруппы должно быть от 2 до 100 символов")
    String name;
}
