package ru.impuls.helpdeskservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.service.impl.FileService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileService fileService;

    @GetMapping("/{filePath}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filePath) {
        var fileResource = fileService.getFileResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResource.contentType()))
                .body(fileResource.resource());
    }

    /*@DeleteMapping("/{filePath}")
    public void deleteFile(@PathVariable String filePath) {
        var fileResource = fileService.getFileResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResource.contentType()))
                .body(fileResource.resource());
    }*/
}
