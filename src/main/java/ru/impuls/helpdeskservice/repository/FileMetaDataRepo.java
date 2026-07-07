package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.impuls.helpdeskservice.entity.FileMetaData;

import java.util.Optional;
import java.util.UUID;

public interface FileMetaDataRepo extends JpaRepository<FileMetaData, UUID>, JpaSpecificationExecutor<FileMetaData> {
    Optional<FileMetaData> findByStoredFileName(String fileName);
}
