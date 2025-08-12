package ru.impuls.helpdeskservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.auth.dto.response.UserResDto;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogItemDto extends BaseDto {
    String text;
    Instant createdDate;
    UserResDto user;
}
