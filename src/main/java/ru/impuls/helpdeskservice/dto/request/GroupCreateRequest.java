package ru.impuls.helpdeskservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
        @NotBlank(message = "Название группы обязательно")
        @Size(min = 1, max = 100, message = "Название группы должно быть от 1 до 100 символов")
        String name,
        @NotNull(message = "Оператор обязателен")
        Long operId
) {}
