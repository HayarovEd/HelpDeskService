package ru.impuls.helpdeskservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatCreateDto {
    @NotBlank
    String name;

    @Email
    String email;

    @NotBlank
    String phone;

    @NotBlank
    String message;
}
