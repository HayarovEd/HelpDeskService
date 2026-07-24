package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HmsMessage(
        @JsonProperty("notification")
        HmsNotification notification,

        @JsonProperty("token")
        List<String> token // Список push-токенов получателей
) {}