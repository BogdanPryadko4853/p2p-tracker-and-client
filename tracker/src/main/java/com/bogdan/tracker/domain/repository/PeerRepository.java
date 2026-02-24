package com.bogdan.tracker.domain.repository;

import com.bogdan.tracker.domain.model.Peer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeerRepository {
    List<Peer> findAll();

    Optional<Peer> findById(UUID id);

    void save(Peer peer);

    void delete(Peer peer);

    boolean existsById(UUID id);

    long count();

    Optional<Peer> findByIpAndPort(String ip, int port);

    List<Peer> findByLastSeenAfter(LocalDateTime since);

    List<Peer> findPeersByFileHash(String hash);

    int deleteByLastSeenBefore(LocalDateTime threshold);
}
