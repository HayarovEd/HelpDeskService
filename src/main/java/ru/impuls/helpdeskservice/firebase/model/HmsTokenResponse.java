package ru.impuls.helpdeskservice.firebase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HmsTokenResponse(
        @JsonProperty("access_token")
        String token,

        @JsonProperty("expires_in")
        Long expiresIn, // Изменено имя для точной семантики (секунды)

        @JsonProperty("token_type")
        String tokenType
) {
}
