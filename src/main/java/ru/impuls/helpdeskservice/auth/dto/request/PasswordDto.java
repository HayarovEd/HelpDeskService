package ru.impuls.helpdeskservice.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordDto {

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and 1 special character"
    )
    @Schema(
            description = "Пароль (минимум 8 символов: 1 заглавная, 1 строчная буква, 1 цифра, 1 спецсимвол)",
            example = "Pas$w0rd",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String password;
}
