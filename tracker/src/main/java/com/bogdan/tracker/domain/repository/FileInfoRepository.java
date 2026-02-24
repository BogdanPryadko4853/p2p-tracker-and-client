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
}

