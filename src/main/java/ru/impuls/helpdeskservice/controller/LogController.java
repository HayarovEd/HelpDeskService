package ru.impuls.helpdeskservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.dto.response.LogDto;
import ru.impuls.helpdeskservice.service.LogService;

import java.time.Instant;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    public LogDto getAllWithFilter(
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int count) {

        return logService.findAllWithFilter(to, count);
    }
}