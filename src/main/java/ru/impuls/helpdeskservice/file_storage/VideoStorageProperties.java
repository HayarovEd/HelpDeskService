package ru.impuls.helpdeskservice.file_storage;

import java.util.Set;

public class VideoStorageProperties  extends MediaStorageProperties{
    public VideoStorageProperties() {
        this.basePath = "./video";
        this.allowedMimeTypes = Set.of( "video/mp4",
                "video/mpeg",
                "video/ogg",
                "video/webm",
                "video/quicktime",
                "video/x-msvideo",
                "video/x-matroska",
                "video/mp2t");
        this.maxFileSize = 100 * 1024 * 1024; // 100 MB
    }
}
