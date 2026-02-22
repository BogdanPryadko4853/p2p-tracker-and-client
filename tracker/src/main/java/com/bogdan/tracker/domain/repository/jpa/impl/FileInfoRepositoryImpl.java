package com.bogdan.tracker.domain.repository.jpa.impl;

import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.repository.BaseRepository;
import com.bogdan.tracker.domain.repository.jpa.FileInfoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileInfoRepositoryImpl implements BaseRepository<FileInfo, String> {

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
    public void deleteById(FileInfo fileInfo) {
        fileInfoJpaRepository.delete(fileInfo);
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
