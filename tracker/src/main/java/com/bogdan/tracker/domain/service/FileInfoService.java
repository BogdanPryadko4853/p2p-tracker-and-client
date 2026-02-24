package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.domain.model.FileInfo;

import java.util.List;
import java.util.Optional;

public interface FileInfoService {

    void saveFile(FileInfo fileInfo);

    void deleteFile(String hash);

    Optional<FileInfo> findFileById(String hash);

    List<FileInfo> findAllFiles();

    boolean existsFile(String hash);

    long countFiles();

    List<FileInfo> searchFilesByName(String query);

    List<FileInfo> findFilesByPeerId(String peerId);

    void updateFileName(String hash, String newName);
}