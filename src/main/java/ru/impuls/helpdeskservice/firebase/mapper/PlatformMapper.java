package ru.impuls.helpdeskservice.firebase.mapper;
/*

import org.springframework.http.HttpStatus;
import ru.impuls.helpdeskservice.entity.Platform;
import ru.impuls.helpdeskservice.exception.AppException;

public interface PlatformMapper {
    default Platform map(String platform) {
        if (platform == null) {
            return null;
        }
        try {
            return Platform.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid platform: " + platform + ". Expected FCM or HMS", HttpStatus.BAD_REQUEST);
        }
    }

    default String map(Platform platform) {
        return platform != null ? platform.name() : null;
    }
}*/
