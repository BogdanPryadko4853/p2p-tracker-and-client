package com.bogdan.tracker.infrastructure.repository;

import com.bogdan.tracker.common.FileInfoUtils;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.infrastructure.repository.jpa.FileInfoJpaRepository;
import com.bogdan.tracker.infrastructure.repository.jpa.impl.FileInfoRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        List<FileInfo> expected = FileInfoUtils.createTwoFiles();
        when(fileInfoJpaRepository.findAll()).thenReturn(expected);

        List<FileInfo> actual = fileInfoRepository.findAll();

        assertEquals(expected, actual);
        verify(fileInfoJpaRepository).findAll();
    }

    @Test
    void findById_shouldReturnFile_whenFileExists() {
        FileInfo expected = FileInfoUtils.createOneFile("hash123");
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
        FileInfo file = FileInfoUtils.createOneFile("hash456");

        fileInfoRepository.save(file);

        verify(fileInfoJpaRepository).save(file);
    }

    @Test
    void save_shouldThrowException_whenDuplicateKey() {
        FileInfo file = FileInfoUtils.createOneFile("hash456");
        when(fileInfoJpaRepository.save(file)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> fileInfoRepository.save(file));
        verify(fileInfoJpaRepository).save(file);
    }

    @Test
    void delete_shouldCallJpaRepositoryDeleteById_whenFileExists() {
        FileInfo file = FileInfoUtils.createOneFile("hash789");

        fileInfoRepository.delete(file);

        verify(fileInfoJpaRepository).deleteById(file.getHash());
        verifyNoMoreInteractions(fileInfoJpaRepository);
    }

    @Test
    void delete_shouldThrowException_whenFileNotFound() {
        FileInfo file = FileInfoUtils.createOneFile("hash789");
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

    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatchingFiles() {
        String searchQuery = "photo";
        List<FileInfo> expected = FileInfoUtils.createTwoFiles();
        when(fileInfoJpaRepository.findByNameContainingIgnoreCase(searchQuery)).thenReturn(expected);

        List<FileInfo> actual = fileInfoRepository.findByNameContainingIgnoreCase(searchQuery);

        assertEquals(expected, actual);
        verify(fileInfoJpaRepository).findByNameContainingIgnoreCase(searchQuery);
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnEmptyList_whenNoMatches() {
        String searchQuery = "nonexistent";
        when(fileInfoJpaRepository.findByNameContainingIgnoreCase(searchQuery)).thenReturn(List.of());

        List<FileInfo> actual = fileInfoRepository.findByNameContainingIgnoreCase(searchQuery);

        assertTrue(actual.isEmpty());
        verify(fileInfoJpaRepository).findByNameContainingIgnoreCase(searchQuery);
    }

    @Test
    void findByPeerId_shouldReturnFilesForPeer() {
        UUID peerId = UUID.randomUUID();
        List<FileInfo> expected = FileInfoUtils.createTwoFiles();
        when(fileInfoJpaRepository.findByPeerId(peerId)).thenReturn(expected);

        List<FileInfo> actual = fileInfoRepository.findByPeerId(peerId);

        assertEquals(expected, actual);
        verify(fileInfoJpaRepository).findByPeerId(peerId);
    }

    @Test
    void findByPeerId_shouldReturnEmptyList_whenPeerHasNoFiles() {
        UUID peerId = UUID.randomUUID();
        when(fileInfoJpaRepository.findByPeerId(peerId)).thenReturn(List.of());

        List<FileInfo> actual = fileInfoRepository.findByPeerId(peerId);

        assertTrue(actual.isEmpty());
        verify(fileInfoJpaRepository).findByPeerId(peerId);
    }

    @Test
    void findByHashAndPeerId_shouldReturnFile_whenPeerHasFile() {
        String hash = "hash123";
        UUID peerId = UUID.randomUUID();
        FileInfo expected = FileInfoUtils.createOneFile(hash);
        when(fileInfoJpaRepository.findByHashAndPeerId(hash, peerId)).thenReturn(Optional.of(expected));

        Optional<FileInfo> actual = fileInfoRepository.findByHashAndPeerId(hash, peerId);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(fileInfoJpaRepository).findByHashAndPeerId(hash, peerId);
    }

    @Test
    void findByHashAndPeerId_shouldReturnEmptyOptional_whenPeerDoesNotHaveFile() {
        String hash = "hash123";
        UUID peerId = UUID.randomUUID();
        when(fileInfoJpaRepository.findByHashAndPeerId(hash, peerId)).thenReturn(Optional.empty());

        Optional<FileInfo> actual = fileInfoRepository.findByHashAndPeerId(hash, peerId);

        assertTrue(actual.isEmpty());
        verify(fileInfoJpaRepository).findByHashAndPeerId(hash, peerId);
    }

    @Test
    void deleteByHash_shouldCallJpaRepositoryDeleteById() {
        String hash = "hash789";

        fileInfoRepository.deleteByHash(hash);

        verify(fileInfoJpaRepository).deleteById(hash);
        verifyNoMoreInteractions(fileInfoJpaRepository);
    }

    @Test
    void deleteByHash_shouldThrowException_whenFileNotFound() {
        String hash = "nonexistent";
        doThrow(DataIntegrityViolationException.class).when(fileInfoJpaRepository).deleteById(hash);

        assertThrows(DataIntegrityViolationException.class, () -> fileInfoRepository.deleteByHash(hash));
        verify(fileInfoJpaRepository).deleteById(hash);
    }
}