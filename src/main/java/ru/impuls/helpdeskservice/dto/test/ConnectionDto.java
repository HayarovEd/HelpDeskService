package ru.impuls.helpdeskservice.dto.test;

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
    Instant start;
    Instant end;
    UserDto user;
    List<MessageDto> messages;
}
