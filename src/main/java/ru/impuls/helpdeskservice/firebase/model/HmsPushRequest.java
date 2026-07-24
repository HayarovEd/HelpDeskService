package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public record HmsPushRequest(
        @JsonProperty("validate_only")
        boolean validateOnly,

        @JsonProperty("message")
        HmsMessage message
) {}
