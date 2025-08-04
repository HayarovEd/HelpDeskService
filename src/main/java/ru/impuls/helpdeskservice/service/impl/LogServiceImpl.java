package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.dto.response.LogDto;
import ru.impuls.helpdeskservice.entity.Log;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.LogMapper;
import ru.impuls.helpdeskservice.repository.LogRepo;
import ru.impuls.helpdeskservice.service.LogService;
import ru.impuls.helpdeskservice.util.AuthUtil;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepo logRepo;
    private final LogMapper logMapper;
    private final UserRepo userRepo;

    @Override
    public List<LogDto> findAllWithFilter(Instant to, int count) {

        if (count < 0 || count > 1000) {
            throw new AppException(
                    "Count must be between 0 and 1000",
                    HttpStatus.BAD_REQUEST
            );
        }

        Pageable pageable = PageRequest.of(0, count, Sort.by("createdDate").descending());

        if (to == null) {
            return logRepo.findAll(pageable)
                    .stream()
                    .map(logMapper::toDto)
                    .toList();
        } else {
            return logRepo.findAllByCreatedDateBefore(to, pageable)
                    .stream()
                    .map(logMapper::toDto)
                    .toList();
        }
    }

    @Override
    public void create(String message) {
        var authenticatedUserId = AuthUtil.getAuthenticatedUser().getId();
        var user = userRepo.findById(authenticatedUserId)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
        var log = Log.builder()
                .text(message)
                .user(user)
                .build();

        logRepo.save(log);
    }
}
