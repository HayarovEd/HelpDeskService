package ru.impuls.helpdeskservice.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.auth.dto.RoleDto;
import ru.impuls.helpdeskservice.dto.BaseDto;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto extends BaseDto {
    String name;
    String email;
    boolean isDisabled;
    RoleDto role;
}
