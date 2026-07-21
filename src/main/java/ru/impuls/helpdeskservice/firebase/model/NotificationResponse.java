package ru.impuls.helpdeskservice.firebase.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationResponse(
        @JsonProperty("status")
        int status,
        @JsonProperty("message")
        String message
) {
}
