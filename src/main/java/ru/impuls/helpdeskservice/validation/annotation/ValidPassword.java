package ru.impuls.helpdeskservice.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.impuls.helpdeskservice.validation.validator.PasswordValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must contain: " +
            "8+ characters, " +
            "1 uppercase letter, " +
            "1 lowercase letter, " +
            "1 digit, " +
            "1 special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
