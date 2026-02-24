package com.bogdan.tracker.domain.service.impl;

import com.bogdan.tracker.domain.exception.Peer.PeerAlreadyExistsException;
import com.bogdan.tracker.domain.exception.Peer.PeerNotFoundException;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.repository.PeerRepository;
import com.bogdan.tracker.domain.service.PeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PeerServiceImpl implements PeerService {

    private final PeerRepository peerRepository;

    @Override
    @Transactional
    public void savePeer(Peer peer) {
        peerRepository.findByIpAndPort(peer.getIp(), peer.getPort())
                .ifPresent(existingPeer -> {
                    throw new PeerAlreadyExistsException(
                            String.format("Peer already exists with ip: %s and port: %d",
                                    peer.getIp(), peer.getPort())
                    );
                });

        if (peer.getLastSeen() == null) {
            peer.setLastSeen(LocalDateTime.now());
        }

        peerRepository.save(peer);
    }

    @Override
    @Transactional
    public void deletePeer(UUID peerId) {
        Peer peer = findPeerById(peerId);
        peerRepository.delete(peer);
        log.info("Peer deleted with id: {}", peerId);
    }

    @Override
    @Transactional
    public void deletePeer(Peer peer) {
        deletePeer(peer.getId());
    }

    @Override
    public List<Peer> findAllPeers() {
        log.debug("Fetching all peers");
        return peerRepository.findAll();
    }

    @Override
    public Peer findPeerById(UUID id) {
        return peerRepository.findById(id)
                .orElseThrow(() -> new PeerNotFoundException("Peer with id " + id + " not found"));
    }

    @Override
    @Transactional
    public Peer updateLastSeenPeer(UUID id) {
        Peer currentPeer = findPeerById(id);
        currentPeer.setLastSeen(LocalDateTime.now());
        log.debug("Updated lastSeen for peer: {}", id);
        return currentPeer;
    }

    @Override
    public Peer findPeerByIpAndPort(String ip, int port) {
        return peerRepository.findByIpAndPort(ip, port)
                .orElseThrow(() -> new PeerNotFoundException(
                        String.format("Peer not found with ip: %s and port: %d", ip, port)
                ));
    }

    @Override
    public List<Peer> findActivePeers(LocalDateTime since) {
        log.debug("Fetching active peers since: {}", since);
        return peerRepository.findByLastSeenAfter(since);
    }

    @Override
    public List<Peer> findPeersByFileHash(String fileHash) {
        log.debug("Fetching peers for file hash: {}", fileHash);
        return peerRepository.findPeersByFileHash(fileHash);
    }

    @Override
    @Transactional
    public void cleanupInactivePeers(LocalDateTime threshold) {
        int deletedCount = peerRepository.deleteByLastSeenBefore(threshold);
        if (deletedCount > 0) {
            log.info("Cleaned up {} inactive peers (last seen before {})", deletedCount, threshold);
        }
    }
}