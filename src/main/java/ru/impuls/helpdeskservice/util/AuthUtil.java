package ru.impuls.helpdeskservice.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;

import java.util.Optional;

@Component
public class AuthUtil  {

    public static User getAuthenticatedUser() {
        return getOptionalAuthenticatedUser()
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_AUTHENTICATED,
                        HttpStatus.UNAUTHORIZED
                ));
    }

    public static Optional<User> getOptionalAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}
