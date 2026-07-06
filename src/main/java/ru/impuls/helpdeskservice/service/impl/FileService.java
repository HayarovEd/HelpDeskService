package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.repository.FileMetaDataRepo;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {
    private final FileMetaDataRepo fileMetaDataRepo;

}
