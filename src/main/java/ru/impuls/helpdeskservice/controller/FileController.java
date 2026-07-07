package ru.impuls.helpdeskservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.impuls.helpdeskservice.service.impl.FileService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileService fileService;

    @GetMapping("/{filePath}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filePath) {
        return ResponseEntity.ok()
                .body(fileService.getFileResource(filePath));
    }
}
