package ru.impuls.helpdeskservice.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.impuls.helpdeskservice.validation.annotation.ValidEmail;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    private static final String EMAIL_REGEX =
            "^[\\w.-]+@[\\w-]+\\.[\\w-]{2,4}(\\.[\\w-]{2,4})?$";

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }
}
