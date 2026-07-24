package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HmsNotification(
        @JsonProperty("title")
        String title,

        @JsonProperty("body")
        String body
) {}