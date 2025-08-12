package ru.impuls.helpdeskservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.auth.dto.response.UserResDto;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionDto extends BaseDto {

    @JsonProperty("start")
    Instant startDate;

    @JsonProperty("end")
    Instant endDate;

    UserResDto user;
    List<MessageDto> messages;
}
