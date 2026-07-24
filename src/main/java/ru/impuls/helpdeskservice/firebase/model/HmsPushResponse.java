package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HmsPushResponse(
        @JsonProperty("code")
        String code,

        @JsonProperty("msg")
        String msg,

        @JsonProperty("requestId")
        String requestId
) {}