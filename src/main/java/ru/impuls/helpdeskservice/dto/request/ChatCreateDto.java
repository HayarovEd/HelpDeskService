package ru.impuls.helpdeskservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatCreateDto {
    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[\\w.-]+@[\\w-]+\\.[\\w-]{2,4}(\\.[\\w-]{2,4})?$",
            message = "Email must be in format: user@domain.com or user@domain.co.uk"
    )
    String email;

    @NotBlank(message = "Phone is required")
    String phone;

    @NotBlank(message = "Message is required")
    String message;
}
