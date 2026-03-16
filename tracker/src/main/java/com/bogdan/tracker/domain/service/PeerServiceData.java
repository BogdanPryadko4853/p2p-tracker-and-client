package com.bogdan.tracker.domain.service;

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
        List<FileInfo> managedNewFiles = convertToManagedFiles(newFiles);
        peer.setFiles(managedNewFiles);
        peerRepository.save(peer);
        log.info("Updated files for peer {}: now {} files", peerId, managedNewFiles.size());
    }

    @Transactional
    public void deletePeer(UUID peerId) {
        Peer peer = findPeerById(peerId);
        peerRepository.delete(peer);
        log.info("Peer deleted with id: {}", peerId);
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

    public List<Peer> findActivePeers(LocalDateTime since) {
        log.debug("Fetching active peers since: {}", since);
        return peerRepository.findByLastSeenAfter(since);
    }

    public List<Peer> findPeersByFileHash(String fileHash) {
        log.debug("Fetching peers for file hash: {}", fileHash);
        return peerRepository.findPeersByFileHash(fileHash);
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