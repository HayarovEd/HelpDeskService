package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HmsAccessTokenRequest(
        @JsonProperty("grant_type")
        String grantType,

        @JsonProperty("client_id")
        String clientId, // Исправлено имя и тип (ID в Huawei передается как строка)

        @JsonProperty("client_secret")
        String clientSecret // Добавлено обязательное поле secrets
) {
}