package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.domain.exception.fileInfo.FileInfoAlreadyExistException;
import com.bogdan.tracker.domain.exception.fileInfo.FileInfoNotFoundException;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.infrastructure.repository.jpa.FileInfoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileInfoServiceData {

    private final FileInfoJpaRepository fileInfoRepository;

    @Transactional
    public FileInfo saveFile(FileInfo fileInfo) {
        if (fileInfoRepository.existsById(fileInfo.getHash())) {
            throw new FileInfoAlreadyExistException("File already exists with hash: " + fileInfo.getHash());
        }
        FileInfo saved = fileInfoRepository.save(fileInfo);
        log.info("File saved with hash: {}", saved.getHash());
        return saved;
    }

    @Transactional
    public void deleteFile(String hash) {
        fileInfoRepository.findById(hash).ifPresentOrElse(
                file -> {
                    fileInfoRepository.delete(file);
                    log.info("File deleted with hash: {}", hash);
                },
                () -> {
                    throw new FileInfoNotFoundException("File does not exist with hash: " + hash);
                }
        );
    }

    public Optional<FileInfo> findFileById(String hash) {
        return fileInfoRepository.findById(hash);
    }


    public List<FileInfo> findAllFiles() {
        log.debug("Fetching all files");
        return fileInfoRepository.findAll();
    }

    public List<FileInfo> searchFilesByName(String query) {
        log.debug("Searching files by name: {}", query);
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return fileInfoRepository.findByNameContainingIgnoreCase(query.trim());
    }

    public List<FileInfo> findFilesByPeerId(UUID peerId) {
        log.debug("Fetching files for peer: {}", peerId);
        return fileInfoRepository.findByPeerId(peerId);
    }
}