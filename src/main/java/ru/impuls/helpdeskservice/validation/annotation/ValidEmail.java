package ru.impuls.helpdeskservice.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.impuls.helpdeskservice.validation.validator.EmailValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    String message() default "Invalid email format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
