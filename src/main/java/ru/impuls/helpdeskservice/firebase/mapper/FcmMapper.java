package ru.impuls.helpdeskservice.firebase.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.http.HttpStatus;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.entity.FireBaseToken;
import ru.impuls.helpdeskservice.entity.Platform;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.firebase.model.TokenRegistrationRequest;

import java.util.Collections;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = Collections.class
)
public interface FcmMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", source = "tokenRegistrationRequest.token")
    @Mapping(target = "deviceId", source = "tokenRegistrationRequest.deviceId")
    @Mapping(target = "deviceName", source = "tokenRegistrationRequest.deviceName")
    @Mapping(target = "platform", expression = "java(mapPlatform(tokenRegistrationRequest.platform()))")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FireBaseToken toEntity(TokenRegistrationRequest tokenRegistrationRequest, User user);

    default Platform mapPlatform(String platform) {
        if (platform == null) {
            return null;
        }
        try {
            return Platform.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid platform: " + platform + ". Expected FCM or HMS", HttpStatus.BAD_REQUEST);
        }
    }
}
