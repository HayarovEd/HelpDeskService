package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.impuls.helpdeskservice.entity.FileMetaData;
import ru.impuls.helpdeskservice.entity.FireBaseToken;

import java.util.UUID;

public interface FireBaseRepo extends JpaRepository<FireBaseToken, UUID>, JpaSpecificationExecutor<FileMetaData> {
}
