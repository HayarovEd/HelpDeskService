package ru.impuls.helpdeskservice.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;

@Component
public class AuthUtil  {

    public static User getAuthenticatedUser() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }

        throw new AppException(
                ErrorMessage.USER_NOT_AUTHENTICATED,
                HttpStatus.UNAUTHORIZED
        );
    }
}
