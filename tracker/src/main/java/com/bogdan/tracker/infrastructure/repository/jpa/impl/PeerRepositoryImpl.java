package com.bogdan.tracker.domain.repository.jpa.impl;

import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.repository.PeerRepository;
import com.bogdan.tracker.domain.repository.jpa.PeerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PeerRepositoryImpl implements PeerRepository{

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
}
