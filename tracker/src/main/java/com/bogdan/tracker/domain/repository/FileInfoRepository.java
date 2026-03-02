package com.bogdan.tracker.domain.repository;

import com.bogdan.tracker.domain.model.FileInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileInfoRepository {
    List<FileInfo> findAll();

    Optional<FileInfo> findById(String hash);

    void save(FileInfo FileInfo);

    void delete(FileInfo FileInfo);

    boolean existsById(String hash);

    long count();

    List<FileInfo> findByNameContainingIgnoreCase(String name);

    List<FileInfo> findByPeerId(UUID peerId);

    Optional<FileInfo> findByHashAndPeerId(String hash, UUID peerId);

    void deleteByHash(String hash);
}

