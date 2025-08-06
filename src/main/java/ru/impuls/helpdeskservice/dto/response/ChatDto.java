package ru.impuls.helpdeskservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.dto.BaseDto;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.ConnectionDto;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatDto extends BaseDto {
    String name;
    String email;
    String phone;
    SubgroupDto subgroup;
    List<ConnectionDto> connections;
}
