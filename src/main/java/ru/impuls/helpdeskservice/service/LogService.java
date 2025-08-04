package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.dto.response.LogDto;

import java.time.Instant;
import java.util.List;

public interface LogService {
    List<LogDto> findAllWithFilter(Instant to, int count);
    void create(String message);
}
