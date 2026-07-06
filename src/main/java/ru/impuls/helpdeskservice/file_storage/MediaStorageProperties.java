package ru.impuls.helpdeskservice.file_storage;


import lombok.Getter;

import java.util.Set;

public abstract class MediaStorageProperties {

    @Getter
    protected String basePath;

    protected Set<String> allowedMimeTypes;

    @Getter
    protected long maxFileSize;

    public boolean supportsMimeType(String mimeType) {
        return mimeType != null && allowedMimeTypes.contains(mimeType);
    }

}
