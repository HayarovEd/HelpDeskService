package ru.impuls.helpdeskservice.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogEntityType {
    GROUP("группа"),
    SUBGROUP("подгруппа"),
    USER("пользователь");

    private final String description;

}