package com.bogdan.tracker.domain.repository;

import com.bogdan.tracker.domain.model.FileInfo;

import java.util.List;
import java.util.Optional;

public interface FileInfoRepository {
    List<FileInfo> findAll();

    Optional<FileInfo> findById(String hash);

    void save(FileInfo FileInfo);

    void delete(FileInfo FileInfo);

    boolean existsById(String hash);

    long count();

    List<FileInfo> findByNameContainingIgnoreCase(String name);

    List<FileInfo> findByPeerId(String peerId);

    Optional<FileInfo> findByHashAndPeerId(String hash, String peerId);

    void deleteByHash(String hash);
}

