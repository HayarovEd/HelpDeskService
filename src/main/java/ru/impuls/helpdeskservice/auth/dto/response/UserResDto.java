package ru.impuls.helpdeskservice.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserResDto extends BaseDto {
    String name;
    String email;
    RoleDto role;

    @JsonProperty("isDisabled")
    boolean disabled;
}
