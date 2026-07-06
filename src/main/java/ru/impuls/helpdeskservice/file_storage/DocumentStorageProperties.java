package ru.impuls.helpdeskservice.file_storage;

import java.util.Set;

public class DocumentStorageProperties extends MediaStorageProperties {
    public DocumentStorageProperties() {
        this.basePath = "./documents";
        this.allowedMimeTypes = Set.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "application/rtf"
        );
        this.maxFileSize = 5 * 1024 * 1024; // 5 MB
    }

}
