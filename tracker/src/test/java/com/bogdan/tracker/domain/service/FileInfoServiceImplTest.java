package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.common.FileInfoUtils;
import com.bogdan.tracker.domain.exception.fileInfo.FileInfoAlreadyExistException;
import com.bogdan.tracker.domain.exception.fileInfo.FileInfoNotFoundException;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.repository.FileInfoRepository;
import com.bogdan.tracker.domain.service.impl.FileInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileInfoServiceImplTest {

    @Mock
    private FileInfoRepository fileInfoRepository;

    @InjectMocks
    private FileInfoServiceImpl fileInfoService;

    @Test
    void saveFile_shouldSave_whenFileDoesNotExist() {
        FileInfo file = FileInfoUtils.createOneFile("hash123");
        when(fileInfoRepository.findById(file.getHash())).thenReturn(Optional.empty());

        fileInfoService.saveFile(file);

        verify(fileInfoRepository).findById(file.getHash());
        verify(fileInfoRepository).save(file);
        verifyNoMoreInteractions(fileInfoRepository);
    }

    @Test
    void saveFile_shouldThrowException_whenFileExists() {
        FileInfo existingFile = FileInfoUtils.createOneFile("hash123");
        when(fileInfoRepository.findById(existingFile.getHash())).thenReturn(Optional.of(existingFile));

        assertThrows(FileInfoAlreadyExistException.class,
                () -> fileInfoService.saveFile(existingFile));

        verify(fileInfoRepository).findById(existingFile.getHash());
        verify(fileInfoRepository, never()).save(any());
        verifyNoMoreInteractions(fileInfoRepository);
    }

    @Test
    void deleteFile_shouldDelete_whenFileExists() {
        FileInfo file = FileInfoUtils.createOneFile("hash123");
        when(fileInfoRepository.findById(file.getHash())).thenReturn(Optional.of(file));

        fileInfoService.deleteFile(file.getHash());

        verify(fileInfoRepository).findById(file.getHash());
        verify(fileInfoRepository).delete(file);
        verifyNoMoreInteractions(fileInfoRepository);
    }

    @Test
    void deleteFile_shouldThrowException_whenFileDoesNotExist() {
        String hash = "nonexistent";
        when(fileInfoRepository.findById(hash)).thenReturn(Optional.empty());

        assertThrows(FileInfoNotFoundException.class,
                () -> fileInfoService.deleteFile(hash));

        verify(fileInfoRepository).findById(hash);
        verify(fileInfoRepository, never()).delete(any());
        verifyNoMoreInteractions(fileInfoRepository);
    }

    @Test
    void findFileById_shouldReturnOptional_whenFileExists() {
        FileInfo file = FileInfoUtils.createOneFile("hash123");
        when(fileInfoRepository.findById(file.getHash())).thenReturn(Optional.of(file));

        Optional<FileInfo> result = fileInfoService.findFileById(file.getHash());

        assertTrue(result.isPresent());
        assertEquals(file, result.get());
        verify(fileInfoRepository).findById(file.getHash());
    }

    @Test
    void findFileById_shouldReturnEmptyOptional_whenFileDoesNotExist() {
        String hash = "hash123";
        when(fileInfoRepository.findById(hash)).thenReturn(Optional.empty());

        Optional<FileInfo> result = fileInfoService.findFileById(hash);

        assertTrue(result.isEmpty());
        verify(fileInfoRepository).findById(hash);
    }

    @Test
    void findAllFiles_shouldReturnListOfFiles() {
        List<FileInfo> expected = FileInfoUtils.createTwoFiles();
        when(fileInfoRepository.findAll()).thenReturn(expected);

        List<FileInfo> result = fileInfoService.findAllFiles();

        assertEquals(expected, result);
        verify(fileInfoRepository).findAll();
    }

    @Test
    void findAllFiles_shouldReturnEmptyList_whenNoFiles() {
        when(fileInfoRepository.findAll()).thenReturn(List.of());

        List<FileInfo> result = fileInfoService.findAllFiles();

        assertTrue(result.isEmpty());
        verify(fileInfoRepository).findAll();
    }

    @Test
    void existsFile_shouldReturnTrue_whenFileExists() {
        String hash = "hash123";
        when(fileInfoRepository.existsById(hash)).thenReturn(true);

        boolean exists = fileInfoService.existsFile(hash);

        assertTrue(exists);
        verify(fileInfoRepository).existsById(hash);
    }

    @Test
    void existsFile_shouldReturnFalse_whenFileDoesNotExist() {
        String hash = "hash123";
        when(fileInfoRepository.existsById(hash)).thenReturn(false);

        boolean exists = fileInfoService.existsFile(hash);

        assertFalse(exists);
        verify(fileInfoRepository).existsById(hash);
    }

    @Test
    void countFiles_shouldReturnNumberOfFiles() {
        when(fileInfoRepository.count()).thenReturn(5L);

        long count = fileInfoService.countFiles();

        assertEquals(5L, count);
        verify(fileInfoRepository).count();
    }

    @Test
    void searchFilesByName_shouldReturnMatchingFiles() {
        String query = "photo";
        List<FileInfo> expected = FileInfoUtils.createTwoFiles();
        when(fileInfoRepository.findByNameContainingIgnoreCase(query)).thenReturn(expected);

        List<FileInfo> result = fileInfoService.searchFilesByName(query);

        assertEquals(expected, result);
        verify(fileInfoRepository).findByNameContainingIgnoreCase(query);
    }

    @Test
    void searchFilesByName_shouldReturnEmptyList_whenQueryIsEmpty() {
        List<FileInfo> result = fileInfoService.searchFilesByName("   ");

        assertTrue(result.isEmpty());
        verify(fileInfoRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchFilesByName_shouldReturnEmptyList_whenQueryIsNull() {
        List<FileInfo> result = fileInfoService.searchFilesByName(null);

        assertTrue(result.isEmpty());
        verify(fileInfoRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void findFilesByPeerId_shouldReturnFilesForPeer() {
        UUID peerId = UUID.randomUUID();
        List<FileInfo> expected = FileInfoUtils.createTwoFiles();
        when(fileInfoRepository.findByPeerId(peerId)).thenReturn(expected);

        List<FileInfo> result = fileInfoService.findFilesByPeerId(peerId);

        assertEquals(expected, result);
        verify(fileInfoRepository).findByPeerId(peerId);
    }

    @Test
    void findFilesByPeerId_shouldReturnEmptyList_whenPeerHasNoFiles() {
        UUID peerId = UUID.randomUUID();
        when(fileInfoRepository.findByPeerId(peerId)).thenReturn(List.of());

        List<FileInfo> result = fileInfoService.findFilesByPeerId(peerId);

        assertTrue(result.isEmpty());
        verify(fileInfoRepository).findByPeerId(peerId);
    }

    @Test
    void updateFileName_shouldUpdate_whenFileExistsAndNameIsValid() {
        FileInfo file = FileInfoUtils.createOneFile("hash123");
        String oldName = file.getName();
        String newName = "newfile.txt";
        when(fileInfoRepository.findById(file.getHash())).thenReturn(Optional.of(file));

        fileInfoService.updateFileName(file.getHash(), newName);

        assertEquals(newName, file.getName());
        verify(fileInfoRepository).findById(file.getHash());
        verify(fileInfoRepository).save(file);
    }

    @Test
    void updateFileName_shouldThrowException_whenFileDoesNotExist() {
        String hash = "hash123";
        when(fileInfoRepository.findById(hash)).thenReturn(Optional.empty());

        assertThrows(FileInfoNotFoundException.class,
                () -> fileInfoService.updateFileName(hash, "newname.txt"));

        verify(fileInfoRepository).findById(hash);
        verify(fileInfoRepository, never()).save(any());
    }

    @Test
    void updateFileName_shouldThrowException_whenNewNameIsNull() {
        FileInfo file = FileInfoUtils.createOneFile("hash123");
        when(fileInfoRepository.findById(file.getHash())).thenReturn(Optional.of(file));

        assertThrows(IllegalArgumentException.class,
                () -> fileInfoService.updateFileName(file.getHash(), null));

        verify(fileInfoRepository).findById(file.getHash());
        verify(fileInfoRepository, never()).save(any());
    }

    @Test
    void updateFileName_shouldThrowException_whenNewNameIsEmpty() {
        FileInfo file = FileInfoUtils.createOneFile("hash123");
        when(fileInfoRepository.findById(file.getHash())).thenReturn(Optional.of(file));

        assertThrows(IllegalArgumentException.class,
                () -> fileInfoService.updateFileName(file.getHash(), "   "));

        verify(fileInfoRepository).findById(file.getHash());
        verify(fileInfoRepository, never()).save(any());
    }
}