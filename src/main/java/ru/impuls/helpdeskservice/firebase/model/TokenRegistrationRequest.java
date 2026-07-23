package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TokenRegistrationRequest(
        @NotBlank(message = "Device token is required")
        @JsonProperty("token")
        String token,
        @JsonProperty("device_id")
        String deviceId,
        @JsonProperty("device_name")
        String deviceName,
        @JsonProperty("platform")
        String platform
) {}
