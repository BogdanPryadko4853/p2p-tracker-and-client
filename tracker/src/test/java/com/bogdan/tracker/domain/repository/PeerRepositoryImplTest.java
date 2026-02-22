package com.bogdan.tracker.domain.repository;

import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.repository.jpa.PeerJpaRepository;
import com.bogdan.tracker.domain.repository.jpa.impl.PeerRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerRepositoryImplTest {

    @Mock
    private PeerJpaRepository peerJpaRepository;

    @InjectMocks
    private PeerRepositoryImpl peerRepository;

    @Test
    void findAll_shouldReturnListOfPeers() {
        List<Peer> expected = createTwoPeers();
        when(peerJpaRepository.findAll()).thenReturn(expected);

        List<Peer> actual = peerRepository.findAll();

        assertEquals(expected, actual);
        verify(peerJpaRepository).findAll();
    }

    @Test
    void findById_shouldReturnPeer_whenPeerExists() {
        Peer expected = createOnePeer();
        when(peerJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(expected));

        Optional<Peer> actual = peerRepository.findById(UUID.randomUUID());

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(peerJpaRepository).findById(any(UUID.class));
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenPeerNotFound() {
        when(peerJpaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Optional<Peer> actual = peerRepository.findById(UUID.randomUUID());

        assertTrue(actual.isEmpty());
        verify(peerJpaRepository).findById(any(UUID.class));
    }

    @Test
    void save_shouldCallJpaRepositorySave() {
        Peer peer = createOnePeer();

        peerRepository.save(peer);

        verify(peerJpaRepository).save(peer);
    }

    @Test
    void save_shouldThrowException_whenDuplicateKey() {
        Peer peer = createOnePeer();
        when(peerJpaRepository.save(peer)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> peerRepository.save(peer));
        verify(peerJpaRepository).save(peer);
    }

    @Test
    void delete_shouldCallJpaRepositoryDeleteById_whenPeerExists() {
        Peer peer = createOnePeer();

        peerRepository.delete(peer);

        verify(peerJpaRepository).deleteById(peer.getId());
        verifyNoMoreInteractions(peerJpaRepository);
    }

    @Test
    void delete_shouldThrowException_whenPeerNotFound() {
        Peer peer = createOnePeer();
        doThrow(DataIntegrityViolationException.class).when(peerJpaRepository).deleteById(peer.getId());

        assertThrows(DataIntegrityViolationException.class, () -> peerRepository.delete(peer));
        verify(peerJpaRepository).deleteById(peer.getId());
    }

    @Test
    void existsById_shouldReturnTrue_whenPeerExists() {
        when(peerJpaRepository.existsById(any(UUID.class))).thenReturn(true);

        boolean exists = peerRepository.existsById(UUID.randomUUID());

        assertTrue(exists);
        verify(peerJpaRepository).existsById(any(UUID.class));
    }

    @Test
    void existsById_shouldReturnFalse_whenPeerDoesNotExist() {
        when(peerJpaRepository.existsById(any(UUID.class))).thenReturn(false);

        boolean exists = peerRepository.existsById(UUID.randomUUID());

        assertFalse(exists);
        verify(peerJpaRepository).existsById(any(UUID.class));
    }

    @Test
    void count_shouldReturnNumberOfPeers() {
        when(peerJpaRepository.count()).thenReturn(5L);

        long count = peerRepository.count();

        assertEquals(5L, count);
        verify(peerJpaRepository).count();
    }

    private List<Peer> createTwoPeers() {
        List<Peer> peers = new ArrayList<>();
        peers.add(createOnePeer());
        peers.add(Peer.builder()
                .id(UUID.randomUUID())
                .port(5678)
                .ip("127.0.0.2")
                .lastSeen(LocalDateTime.now())
                .build());
        return peers;
    }

    private Peer createOnePeer() {
        return Peer.builder()
                .id(UUID.randomUUID())
                .port(1234)
                .ip("127.0.0.1")
                .lastSeen(LocalDateTime.now())
                .build();
    }
}