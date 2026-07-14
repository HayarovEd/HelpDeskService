package ru.impuls.helpdeskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidateToken(
        @JsonProperty("is_valid")
        Boolean isValid
) {}
