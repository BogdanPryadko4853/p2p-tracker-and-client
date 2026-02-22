package com.bogdan.tracker.domain.repository;

import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.repository.jpa.FileInfoJpaRepository;
import com.bogdan.tracker.domain.repository.jpa.impl.FileInfoRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileInfoRepositoryTest {

    @Mock
    private FileInfoJpaRepository fileInfoJpaRepository;

    @InjectMocks
    private FileInfoRepositoryImpl fileInfoRepository;

    @Test
    void findAll_shouldReturnListOfFiles() {
        List<FileInfo> expected = createTwoFiles();
        when(fileInfoJpaRepository.findAll()).thenReturn(expected);

        List<FileInfo> actual = fileInfoRepository.findAll();

        assertEquals(expected, actual);
        verify(fileInfoJpaRepository).findAll();
    }

    @Test
    void findById_shouldReturnFile_whenFileExists() {
        FileInfo expected = createOneFile("hash123");
        when(fileInfoJpaRepository.findById("hash123")).thenReturn(Optional.of(expected));

        Optional<FileInfo> actual = fileInfoRepository.findById("hash123");

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(fileInfoJpaRepository).findById("hash123");
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenFileNotFound() {
        when(fileInfoJpaRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<FileInfo> actual = fileInfoRepository.findById("nonexistent");

        assertTrue(actual.isEmpty());
        verify(fileInfoJpaRepository).findById("nonexistent");
    }

    @Test
    void save_shouldCallJpaRepositorySave() {
        FileInfo file = createOneFile("hash456");

        fileInfoRepository.save(file);

        verify(fileInfoJpaRepository).save(file);
    }

    @Test
    void save_shouldThrowException_whenDuplicateKey() {
        FileInfo file = createOneFile("hash456");
        when(fileInfoJpaRepository.save(file)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> fileInfoRepository.save(file));
        verify(fileInfoJpaRepository).save(file);
    }

    @Test
    void delete_shouldCallJpaRepositoryDeleteById_whenFileExists() {
        FileInfo file = createOneFile("hash789");

        fileInfoRepository.delete(file);

        verify(fileInfoJpaRepository).deleteById(file.getHash());
        verifyNoMoreInteractions(fileInfoJpaRepository);
    }

    @Test
    void delete_shouldThrowException_whenFileNotFound() {
        FileInfo file = createOneFile("hash789");
        doThrow(DataIntegrityViolationException.class).when(fileInfoJpaRepository).deleteById(file.getHash());

        assertThrows(DataIntegrityViolationException.class, () -> fileInfoRepository.delete(file));
        verify(fileInfoJpaRepository).deleteById(file.getHash());
    }

    @Test
    void existsById_shouldReturnTrue_whenFileExists() {
        when(fileInfoJpaRepository.existsById(anyString())).thenReturn(true);

        boolean exists = fileInfoRepository.existsById("hash123");

        assertTrue(exists);
        verify(fileInfoJpaRepository).existsById("hash123");
    }

    @Test
    void existsById_shouldReturnFalse_whenFileDoesNotExist() {
        when(fileInfoJpaRepository.existsById(anyString())).thenReturn(false);

        boolean exists = fileInfoRepository.existsById("hash123");

        assertFalse(exists);
        verify(fileInfoJpaRepository).existsById("hash123");
    }

    @Test
    void count_shouldReturnNumberOfFiles() {
        when(fileInfoJpaRepository.count()).thenReturn(3L);

        long count = fileInfoRepository.count();

        assertEquals(3L, count);
        verify(fileInfoJpaRepository).count();
    }

    private List<FileInfo> createTwoFiles() {
        return List.of(
                createOneFile("hash1"),
                createOneFile("hash2")
        );
    }

    private FileInfo createOneFile(String hash) {
        return FileInfo.builder()
                .hash(hash)
                .name("file_" + hash + ".txt")
                .size(1024L)
                .build();
    }
}