package ru.impuls.helpdeskservice.dto.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;
import ru.impuls.helpdeskservice.dto.BaseDto;

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

    UserDto user;
    List<MessageDto> messages;
}
