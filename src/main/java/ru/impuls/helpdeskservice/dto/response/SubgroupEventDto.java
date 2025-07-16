package ru.impuls.helpdeskservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.dto.BaseDto;
import ru.impuls.helpdeskservice.dto.GroupForSubDto;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubgroupEventDto extends BaseDto {
    String name;
    GroupForSubDto group;
}
