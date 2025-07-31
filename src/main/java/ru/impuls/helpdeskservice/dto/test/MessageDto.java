package ru.impuls.helpdeskservice.dto.test;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.dto.BaseDto;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageDto extends BaseDto {
    String text;
    Instant date;
    boolean isSupport;
}
