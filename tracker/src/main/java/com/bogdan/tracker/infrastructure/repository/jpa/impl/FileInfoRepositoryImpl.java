package com.bogdan.tracker.infrastructure.repository.jpa.impl;

import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.repository.FileInfoRepository;
import com.bogdan.tracker.infrastructure.repository.jpa.FileInfoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileInfoRepositoryImpl implements FileInfoRepository {

    private final FileInfoJpaRepository fileInfoJpaRepository;

    @Override
    public List<FileInfo> findAll() {
        return fileInfoJpaRepository.findAll();
    }

    @Override
    public Optional<FileInfo> findById(String hash) {
        return fileInfoJpaRepository.findById(hash);
    }

    @Override
    public void save(FileInfo fileInfo) {
        fileInfoJpaRepository.save(fileInfo);
    }

    @Override
    public void delete(FileInfo fileInfo) {
        fileInfoJpaRepository.deleteById(fileInfo.getHash());
    }

    @Override
    public boolean existsById(String hash) {
        return fileInfoJpaRepository.existsById(hash);
    }

    @Override
    public long count() {
        return fileInfoJpaRepository.count();
    }
}
