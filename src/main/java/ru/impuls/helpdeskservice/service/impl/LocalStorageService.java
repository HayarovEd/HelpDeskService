package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.repository.FileMetaDataRepo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalStorageService {
    private static final String FILES_UPLOAD_DIR = "./files";

    @Transactional
    public String storeFile(
            InputStream inputStream,
            String originalFilename,
            String basePath
    ) {
        try {
            Path uploadPath = Paths.get(FILES_UPLOAD_DIR + "/" + basePath);
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String newFilename = UUID.randomUUID() + extension;
            OutputStream outputStream = Files.newOutputStream(uploadPath.resolve(newFilename), StandardOpenOption.CREATE_NEW);
            StreamUtils.copy(inputStream, outputStream);
            return FILES_UPLOAD_DIR + "/" + basePath + "/" + newFilename;
        } catch (
                IOException e
        ) {
            throw new AppException("Ошибка записи файла" + originalFilename + "в хранилище", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteFile(
            String fileName,
            String basePath
    ) {
        Path filePath = Paths.get(FILES_UPLOAD_DIR + "/" + basePath);
        try {
            Files.deleteIfExists(filePath.resolve(fileName));
        } catch (IOException e) {
            throw new AppException("Ошибка уделения файла" + fileName, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Resource loadFilePath(
            String fileName,
            String basePath
    ) throws MalformedURLException {
        Path filePath = Paths.get(FILES_UPLOAD_DIR + "/" + basePath)
                .resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new AppException("Файл не найден: " + fileName, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new UrlResource(filePath.toUri());
    }
}
