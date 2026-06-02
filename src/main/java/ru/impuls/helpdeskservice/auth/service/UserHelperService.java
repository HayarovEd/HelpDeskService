package ru.impuls.helpdeskservice.auth.service;

import ru.impuls.helpdeskservice.auth.dto.response.UserBillingRequest;
import ru.impuls.helpdeskservice.auth.entity.User;

public interface UserHelperService {
    User updateExistingUser(User user, UserBillingRequest response);

    User createNewUser(UserBillingRequest response);
}
