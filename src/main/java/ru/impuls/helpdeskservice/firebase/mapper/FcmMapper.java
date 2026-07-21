package ru.impuls.helpdeskservice.firebase.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.entity.FireBaseToken;

import java.util.Collections;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = Collections.class
)
public interface FcmMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", source = "token")
    @Mapping(target = "user", source = "user")
    FireBaseToken toEntity(String token, User user);
}
