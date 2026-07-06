package ru.impuls.helpdeskservice.file_storage;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Getter
@Component
public class ImageStorageProperties extends MediaStorageProperties {
    public ImageStorageProperties() {
        this.basePath = "./images";
        this.allowedMimeTypes = Set.of(
                "image/jpg",
                "image/jpeg",
                "image/png",
                "image/webp",
                "image/gif",
                "image/bmp",
                "image/svg+xml");
        this.maxFileSize = 10 * 1024 * 1024; // 10 MB
    }

}
