package com.bogdan.tracker.domain.service.impl;

import com.bogdan.tracker.domain.exception.fileInfo.FileInfoAlreadyExistException;
import com.bogdan.tracker.domain.exception.fileInfo.FileInfoNotFoundException;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.repository.FileInfoRepository;
import com.bogdan.tracker.domain.service.FileInfoService;
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
public class FileInfoServiceImpl implements FileInfoService {

    private final FileInfoRepository fileInfoRepository;

    @Override
    @Transactional
    public void saveFile(FileInfo fileInfo) {
        fileInfoRepository.findById(fileInfo.getHash()).ifPresent(existingFile -> {
            throw new FileInfoAlreadyExistException("File already exists with hash: " + fileInfo.getHash());
        });
        fileInfoRepository.save(fileInfo);
        log.info("File saved with hash: {}", fileInfo.getHash());
    }

    @Override
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

    @Override
    public Optional<FileInfo> findFileById(String hash) {
        return fileInfoRepository.findById(hash);
    }

    @Override
    public List<FileInfo> findAllFiles() {
        log.debug("Fetching all files");
        return fileInfoRepository.findAll();
    }

    @Override
    public boolean existsFile(String hash) {
        return fileInfoRepository.existsById(hash);
    }

    @Override
    public long countFiles() {
        return fileInfoRepository.count();
    }

    @Override
    public List<FileInfo> searchFilesByName(String query) {
        log.debug("Searching files by name: {}", query);
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return fileInfoRepository.findByNameContainingIgnoreCase(query.trim());
    }

    @Override
    public List<FileInfo> findFilesByPeerId(UUID peerId) {
        log.debug("Fetching files for peer: {}", peerId);
        return fileInfoRepository.findByPeerId(peerId);
    }

    @Override
    @Transactional
    public void updateFileName(String hash, String newName) {
        FileInfo fileInfo = fileInfoRepository.findById(hash)
                .orElseThrow(() -> new FileInfoNotFoundException("File not found with hash: " + hash));

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New file name cannot be empty");
        }

        String oldName = fileInfo.getName();
        fileInfo.setName(newName.trim());
        fileInfoRepository.save(fileInfo);
        log.info("File name updated for hash: {} from '{}' to '{}'", hash, oldName, newName);
    }
}