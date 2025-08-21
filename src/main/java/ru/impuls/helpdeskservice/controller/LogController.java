package ru.impuls.helpdeskservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.dto.response.LogResponse;
import ru.impuls.helpdeskservice.service.LogService;

import java.time.Instant;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public LogResponse getAll(@RequestParam(required = false) Instant to,
                              @RequestParam(defaultValue = "100") int count) {
        return logService.getAll(to, count);
    }
}