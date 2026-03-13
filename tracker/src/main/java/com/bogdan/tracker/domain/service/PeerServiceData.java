package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.domain.exception.Peer.PeerAlreadyExistsException;
import com.bogdan.tracker.domain.exception.Peer.PeerNotFoundException;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.infrastructure.repository.jpa.PeerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PeerServiceData {

    private final PeerJpaRepository peerRepository;
    private final FileInfoServiceData fileInfoService;

    @Transactional
    public UUID registerPeer(Peer peer) {
        Peer existingPeer = findPeerByIpAndPort(peer.getIp(), peer.getPort());
        if (existingPeer == null) {
            savePeer(peer);
            return peer.getId();
        }
        log.info("Peer already exists: {}", existingPeer.getIp());
        if (!existingPeer.getFiles().equals(peer.getFiles())) {
            peer.getFiles().forEach(newFile -> {
                if (!existingPeer.getFiles().contains(newFile)) {
                    fileInfoService.updateFile(newFile);
                }
            });
        }
        return existingPeer.getId();
    }

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

        if (peer.getFiles() != null && !peer.getFiles().isEmpty()) {
            List<FileInfo> managedFiles = new ArrayList<>();
            for (FileInfo file : peer.getFiles()) {
                Optional<FileInfo> existing = fileInfoService.findFileById(file.getHash());
                if (existing.isPresent()) {
                    managedFiles.add(existing.get());
                } else {
                    FileInfo saved = fileInfoService.saveFile(file);
                    managedFiles.add(saved);
                }
            }
            peer.setFiles(managedFiles);
        }

        peerRepository.save(peer);
    }

    @Transactional
    public void deletePeer(UUID peerId) {
        Peer peer = findPeerById(peerId);
        peerRepository.delete(peer);
        log.info("Peer deleted with id: {}", peerId);
    }

    @Transactional
    public void deletePeer(Peer peer) {
        deletePeer(peer.getId());
    }

    public List<Peer> findAllPeers() {
        log.debug("Fetching all peers");
        return peerRepository.findAll();
    }

    public Peer findPeerById(UUID id) {
        return peerRepository.findById(id)
                .orElseThrow(() -> new PeerNotFoundException("Peer with id " + id + " not found"));
    }

    @Transactional
    public Peer updateLastSeenPeer(UUID id) {
        Peer currentPeer = findPeerById(id);
        currentPeer.setLastSeen(LocalDateTime.now());
        log.debug("Updated lastSeen for peer: {}", id);
        return currentPeer;
    }

    public Peer findPeerByIpAndPort(String ip, int port) {
        return peerRepository.findByIpAndPort(ip, port)
                .orElseThrow(() -> new PeerNotFoundException(
                        String.format("Peer not found with ip: %s and port: %d", ip, port)
                ));
    }

    public List<Peer> findActivePeers(LocalDateTime since) {
        log.debug("Fetching active peers since: {}", since);
        return peerRepository.findByLastSeenAfter(since);
    }

    public List<Peer> findPeersByFileHash(String fileHash) {
        log.debug("Fetching peers for file hash: {}", fileHash);
        return peerRepository.findPeersByFileHash(fileHash);
    }

    @Transactional
    public void cleanupInactivePeers(LocalDateTime threshold) {
        int deletedCount = peerRepository.deleteByLastSeenBefore(threshold);
        if (deletedCount > 0) {
            log.info("Cleaned up {} inactive peers (last seen before {})", deletedCount, threshold);
        }
    }
}