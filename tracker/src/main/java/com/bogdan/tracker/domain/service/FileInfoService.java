package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.domain.model.FileInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileInfoService {

    FileInfo updateFile(FileInfo fileInfo);

    FileInfo saveFile(FileInfo fileInfo);

    void deleteFile(String hash);

    Optional<FileInfo> findFileById(String hash);

    List<FileInfo> findAllFiles();

    boolean existsFile(String hash);

    long countFiles();

    List<FileInfo> searchFilesByName(String query);

    List<FileInfo> findFilesByPeerId(UUID peerId);

    void updateFileName(String hash, String newName);

}