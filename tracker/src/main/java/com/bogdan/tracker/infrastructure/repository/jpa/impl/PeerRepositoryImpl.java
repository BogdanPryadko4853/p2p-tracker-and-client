package com.bogdan.tracker.infrastructure.repository.jpa.impl;

import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.repository.PeerRepository;
import com.bogdan.tracker.infrastructure.repository.jpa.PeerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PeerRepositoryImpl implements PeerRepository {

    private final PeerJpaRepository peerJpaRepository;

    @Override
    public List<Peer> findAll() {
        return peerJpaRepository.findAll();
    }

    @Override
    public Optional<Peer> findById(UUID uuid) {
        return peerJpaRepository.findById(uuid);
    }

    @Override
    public void save(Peer peer) {
        peerJpaRepository.save(peer);
    }

    @Override
    public void delete(Peer peer) {
        peerJpaRepository.deleteById(peer.getId());
    }

    @Override
    public boolean existsById(UUID uuid) {
        return peerJpaRepository.existsById(uuid);
    }

    @Override
    public long count() {
        return peerJpaRepository.count();
    }

    @Override
    public Optional<Peer> findByIpAndPort(String ip, int port) {
        return peerJpaRepository.findByIpAndPort(ip, port);
    }

    @Override
    public List<Peer> findByLastSeenAfter(LocalDateTime since) {
        return peerJpaRepository.findByLastSeenAfter(since);
    }

    @Override
    public List<Peer> findPeersByFileHash(String hash) {
        return peerJpaRepository.findPeersByFileHash(hash);
    }

    @Override
    public int deleteByLastSeenBefore(LocalDateTime threshold) {
        return peerJpaRepository.deleteByLastSeenBefore(threshold);
    }
}