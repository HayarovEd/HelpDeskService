package ru.impuls.helpdeskservice.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.auth.dto.RoleDto;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEditDto extends LoginDto {

    @NotBlank(message = "Name is required")
    String name;

    @JsonProperty("isDisabled")
    boolean disabled;

    RoleDto role;
}
