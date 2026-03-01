package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.common.PeerUtils;
import com.bogdan.tracker.domain.exception.Peer.PeerAlreadyExistsException;
import com.bogdan.tracker.domain.exception.Peer.PeerNotFoundException;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.repository.PeerRepository;
import com.bogdan.tracker.domain.service.impl.PeerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerServiceImplTest {

    @Mock
    PeerRepository peerRepository;

    @InjectMocks
    PeerServiceImpl peerService;

    @Test
    void savePeer_shouldSave_whenPeerDoesNotExist() {
        Peer peerToSave = PeerUtils.createOnePeer();
        when(peerRepository.findByIpAndPort(peerToSave.getIp(), peerToSave.getPort()))
                .thenReturn(Optional.empty());

        peerService.savePeer(peerToSave);

        verify(peerRepository).findByIpAndPort(peerToSave.getIp(), peerToSave.getPort());
        verify(peerRepository).save(peerToSave);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void savePeer_shouldThrowException_whenPeerExists() {
        Peer peerToSave = PeerUtils.createOnePeer();
        when(peerRepository.findByIpAndPort(peerToSave.getIp(), peerToSave.getPort()))
                .thenReturn(Optional.of(peerToSave));

        assertThrows(PeerAlreadyExistsException.class, () -> peerService.savePeer(peerToSave));

        verify(peerRepository).findByIpAndPort(peerToSave.getIp(), peerToSave.getPort());
        verify(peerRepository, never()).save(any());
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void savePeer_shouldSetLastSeen_whenNotProvided() {
        Peer peerToSave = PeerUtils.createOnePeer();
        peerToSave.setLastSeen(null);
        when(peerRepository.findByIpAndPort(peerToSave.getIp(), peerToSave.getPort()))
                .thenReturn(Optional.empty());

        peerService.savePeer(peerToSave);

        assertNotNull(peerToSave.getLastSeen());
        verify(peerRepository).save(peerToSave);
    }

    @Test
    void deletePeer_shouldDelete_whenPeerExists() {
        Peer peerToDelete = PeerUtils.createOnePeer();
        when(peerRepository.findById(peerToDelete.getId())).thenReturn(Optional.of(peerToDelete));

        peerService.deletePeer(peerToDelete);

        verify(peerRepository).findById(peerToDelete.getId());
        verify(peerRepository).delete(peerToDelete);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void deletePeer_shouldThrowException_whenPeerDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        when(peerRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(PeerNotFoundException.class, () -> peerService.deletePeer(randomId));

        verify(peerRepository).findById(randomId);
        verify(peerRepository, never()).delete(any());
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void deletePeer_withPeerObject_shouldDelegateToDeleteById() {
        Peer peerToDelete = PeerUtils.createOnePeer();
        when(peerRepository.findById(peerToDelete.getId())).thenReturn(Optional.of(peerToDelete));

        peerService.deletePeer(peerToDelete);

        verify(peerRepository).findById(peerToDelete.getId());
        verify(peerRepository).delete(peerToDelete);
    }

    @Test
    void findAllPeers_shouldReturnListOfPeers() {
        List<Peer> peers = PeerUtils.createTwoPeers();
        when(peerRepository.findAll()).thenReturn(peers);

        List<Peer> result = peerService.findAllPeers();

        assertEquals(peers, result);
        verify(peerRepository).findAll();
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findAllPeers_shouldReturnEmptyList_whenNoPeers() {
        when(peerRepository.findAll()).thenReturn(Collections.emptyList());

        List<Peer> result = peerService.findAllPeers();

        assertTrue(result.isEmpty());
        verify(peerRepository).findAll();
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findPeerById_shouldReturnPeer_whenExists() {
        Peer peerToFind = PeerUtils.createOnePeer();
        when(peerRepository.findById(peerToFind.getId())).thenReturn(Optional.of(peerToFind));

        Peer result = peerService.findPeerById(peerToFind.getId());

        assertEquals(peerToFind, result);
        verify(peerRepository).findById(peerToFind.getId());
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findPeerById_shouldThrowException_whenPeerDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        when(peerRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(PeerNotFoundException.class, () -> peerService.findPeerById(randomId));

        verify(peerRepository).findById(randomId);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void updateLastSeenPeer_shouldUpdate_whenPeerExists() {
        Peer peerToUpdate = PeerUtils.createOnePeer();
        LocalDateTime originalLastSeen = peerToUpdate.getLastSeen();
        when(peerRepository.findById(peerToUpdate.getId())).thenReturn(Optional.of(peerToUpdate));

        Peer updatedPeer = peerService.updateLastSeenPeer(peerToUpdate.getId());
        assertTrue(updatedPeer.getLastSeen().isAfter(originalLastSeen));
        verify(peerRepository).findById(peerToUpdate.getId());
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void updateLastSeenPeer_shouldThrowException_whenPeerDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        when(peerRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(PeerNotFoundException.class, () -> peerService.updateLastSeenPeer(randomId));

        verify(peerRepository).findById(randomId);
        verify(peerRepository, never()).save(any());
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findPeerByIpAndPort_shouldReturnPeer_whenExists() {
        Peer peerToFind = PeerUtils.createOnePeer();
        when(peerRepository.findByIpAndPort(peerToFind.getIp(), peerToFind.getPort()))
                .thenReturn(Optional.of(peerToFind));

        Peer result = peerService.findPeerByIpAndPort(peerToFind.getIp(), peerToFind.getPort());

        assertEquals(peerToFind, result);
        verify(peerRepository).findByIpAndPort(peerToFind.getIp(), peerToFind.getPort());
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findPeerByIpAndPort_shouldThrowException_whenPeerDoesNotExist() {
        String ip = "192.168.1.100";
        int port = 9999;
        when(peerRepository.findByIpAndPort(ip, port)).thenReturn(Optional.empty());

        assertThrows(PeerNotFoundException.class, () -> peerService.findPeerByIpAndPort(ip, port));

        verify(peerRepository).findByIpAndPort(ip, port);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findActivePeers_shouldReturnListOfActivePeers() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<Peer> activePeers = PeerUtils.createTwoPeers();
        when(peerRepository.findByLastSeenAfter(since)).thenReturn(activePeers);

        List<Peer> result = peerService.findActivePeers(since);

        assertEquals(activePeers, result);
        verify(peerRepository).findByLastSeenAfter(since);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findActivePeers_shouldReturnEmptyList_whenNoActivePeers() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        when(peerRepository.findByLastSeenAfter(since)).thenReturn(Collections.emptyList());

        List<Peer> result = peerService.findActivePeers(since);

        assertTrue(result.isEmpty());
        verify(peerRepository).findByLastSeenAfter(since);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findPeersByFileHash_shouldReturnListOfPeers() {
        String fileHash = "abc123";
        List<Peer> peers = PeerUtils.createTwoPeers();
        when(peerRepository.findPeersByFileHash(fileHash)).thenReturn(peers);

        List<Peer> result = peerService.findPeersByFileHash(fileHash);

        assertEquals(peers, result);
        verify(peerRepository).findPeersByFileHash(fileHash);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void findPeersByFileHash_shouldReturnEmptyList_whenNoPeers() {
        String fileHash = "nonexistent";
        when(peerRepository.findPeersByFileHash(fileHash)).thenReturn(Collections.emptyList());

        List<Peer> result = peerService.findPeersByFileHash(fileHash);

        assertTrue(result.isEmpty());
        verify(peerRepository).findPeersByFileHash(fileHash);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void cleanupInactivePeers_shouldDeleteInactivePeers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        int expectedDeleted = 3;
        when(peerRepository.deleteByLastSeenBefore(threshold)).thenReturn(expectedDeleted);

        peerService.cleanupInactivePeers(threshold);

        verify(peerRepository).deleteByLastSeenBefore(threshold);
        verifyNoMoreInteractions(peerRepository);
    }

    @Test
    void cleanupInactivePeers_shouldDoNothing_whenNoInactivePeers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        when(peerRepository.deleteByLastSeenBefore(threshold)).thenReturn(0);

        peerService.cleanupInactivePeers(threshold);

        verify(peerRepository).deleteByLastSeenBefore(threshold);
        verifyNoMoreInteractions(peerRepository);
    }
}