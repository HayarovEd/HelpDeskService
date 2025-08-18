package ru.impuls.helpdeskservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubgroupCreateRequest (
        @NotBlank(message = "Название подгруппы обязательно")
        @Size(min = 1, max = 100, message = "Название подгруппы должно быть от 1 до 100 символов")
        String name
) {}