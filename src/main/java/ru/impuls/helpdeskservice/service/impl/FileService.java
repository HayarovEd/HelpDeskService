package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.impuls.helpdeskservice.entity.FileMetaData;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.file_storage.MediaStorageProperties;
import ru.impuls.helpdeskservice.repository.FileMetaDataRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {
    private final FileMetaDataRepo fileMetaDataRepo;
    private final LocalStorageService localStorageService;
    private final List<MediaStorageProperties> storagePropertiesList;


    public void storeFile(
            MultipartFile file,
            Message message
    ) {
        MediaStorageProperties properties = validateFile(file);
        var originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new AppException("Некорректный файл", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize()>properties.getMaxFileSize()) {
            throw new AppException("Превышен максимальный размер файла", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        try {
            file.getInputStream().readAllBytes();
            var fileName = localStorageService.storeFile(
                    file.getInputStream(),
                    originalFilename,
                    properties.getBasePath()
            );
            var fileMetaData = FileMetaData
                    .builder()
                    .originalFileName(originalFilename)
                    .storedFileName(fileName)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .createdDate(Instant.now())
                    .message(message)
                    .build();
            fileMetaDataRepo.save(fileMetaData);
        } catch (IOException e) {
            throw new AppException("Ошибки в файле", HttpStatus.BAD_REQUEST);
        }
    }

    public Resource getFileResource(String path) {
        try {
            Path filePath = Paths.get(path);

            if (!Files.exists(filePath)) {
                throw new AppException("Файл не найден: " + path, HttpStatus.NOT_FOUND);
            }

            return new FileSystemResource(filePath);
        } catch (Exception e) {
            throw new AppException("Ошибка при открытии файла:"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private MediaStorageProperties validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException("Файл не содержит данных", HttpStatus.BAD_REQUEST);
        }
        String mimeType = file.getContentType();
        if (mimeType == null) {
            throw new AppException("MIME-тип не указан", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return storagePropertiesList.stream()
                .filter(properties -> properties.supportsMimeType(mimeType))
                .findFirst()
                .orElseThrow(() -> new AppException("Неподдерживаемый MIME-тип", HttpStatus.UNSUPPORTED_MEDIA_TYPE));
    }
}
