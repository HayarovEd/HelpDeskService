package ru.impuls.helpdeskservice.file_storage;


import java.util.Set;

public abstract class MediaStorageProperties {

    protected String basePath;

    protected Set<String> allowedMimeTypes;

    protected long maxFileSize;

}
