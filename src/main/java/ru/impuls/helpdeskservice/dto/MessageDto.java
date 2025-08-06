package ru.impuls.helpdeskservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageDto extends BaseDto {
    String text;

    @JsonProperty("date")
    Instant createdDate;

    @JsonProperty("isSupport")
    boolean support;
}
