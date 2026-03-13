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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PeerServiceData {

    private final PeerJpaRepository peerRepository;
    private final FileInfoServiceData fileInfoService;

    @Transactional
    public UUID registerPeer(Peer peer) {
        Optional<Peer> existingPeerOpt = peerRepository.findByIpAndPort(peer.getIp(), peer.getPort());

        if (existingPeerOpt.isPresent()) {
            Peer existingPeer = existingPeerOpt.get();
            log.info("Peer already exists with id: {}", existingPeer.getId());
            existingPeer.setLastSeen(LocalDateTime.now());
            updatePeerFiles(existingPeer.getId(), peer.getFiles());
            return existingPeer.getId();
        }

        prepareNewPeer(peer);
        Peer savedPeer = peerRepository.save(peer);
        log.info("New peer registered with id: {}", savedPeer.getId());
        return savedPeer.getId();
    }

    @Transactional
    public void removeFileFromPeer(UUID peerId, String fileHash) {
        Peer peer = findPeerById(peerId);

        boolean removed = peer.getFiles().removeIf(f -> f.getHash().equals(fileHash));

        if (removed) {
            peerRepository.save(peer);
            log.info("File {} removed from peer {}", fileHash, peerId);
        }
    }

    @Transactional
    public void updatePeerFiles(UUID peerId, List<FileInfo> newFiles) {
        Peer peer = findPeerById(peerId);

        Set<String> oldHashes = peer.getFiles().stream()
                .map(FileInfo::getHash)
                .collect(Collectors.toSet());

        List<FileInfo> managedNewFiles = convertToManagedFiles(newFiles);

        peer.getFiles().clear();
        peer.getFiles().addAll(managedNewFiles);

        peerRepository.saveAndFlush(peer);

        Set<String> newHashes = managedNewFiles.stream()
                .map(FileInfo::getHash)
                .collect(Collectors.toSet());

        oldHashes.stream()
                .filter(hash -> !newHashes.contains(hash))
                .forEach(hash -> {
                    List<Peer> otherPeers = peerRepository.findPeersByFileHash(hash)
                            .stream()
                            .filter(p -> !p.getId().equals(peerId))
                            .toList();
                    if (otherPeers.isEmpty()) {
                        fileInfoService.deleteFile(hash);
                        log.info("Orphaned file {} deleted from DB", hash);
                    }
                });
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
        prepareNewPeer(peer);
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

    public Optional<Peer> findPeerByIpAndPortOpt(String ip, int port) {
        return peerRepository.findByIpAndPort(ip, port);
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

    private void prepareNewPeer(Peer peer) {
        if (peer.getLastSeen() == null) {
            peer.setLastSeen(LocalDateTime.now());
        }
        if (peer.getFiles() != null && !peer.getFiles().isEmpty()) {
            peer.setFiles(convertToManagedFiles(peer.getFiles()));
        }
    }

    private List<FileInfo> convertToManagedFiles(List<FileInfo> files) {
        List<FileInfo> managed = new ArrayList<>();
        for (FileInfo file : files) {
            Optional<FileInfo> existing = fileInfoService.findFileById(file.getHash());
            if (existing.isPresent()) {
                managed.add(existing.get());
            } else {
                FileInfo saved = fileInfoService.saveFile(file);
                managed.add(saved);
            }
        }
        return managed;
    }
}