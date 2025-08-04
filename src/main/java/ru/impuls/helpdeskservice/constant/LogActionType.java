package ru.impuls.helpdeskservice.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogActionType {
    CREATED("Создана"),
    UPDATED("Обновлена"),
    DELETED("Удалена");

    private final String description;
}
