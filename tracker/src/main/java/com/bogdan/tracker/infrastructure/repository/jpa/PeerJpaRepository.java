package com.bogdan.tracker.infrastructure.repository.jpa;

import com.bogdan.tracker.domain.model.Peer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeerJpaRepository extends JpaRepository<Peer, UUID> {

    Optional<Peer> findByIpAndPort(String ip, int port);

    List<Peer> findByLastSeenAfter(LocalDateTime lastSeenAfter);

    @Query("SELECT p FROM Peer p JOIN p.files f WHERE f.hash = :fileHash")
    List<Peer> findPeersByFileHash(@Param("fileHash") String fileHash);

    @Modifying
    @Transactional
    @Query("DELETE FROM Peer p WHERE p.lastSeen < :threshold")
    int deleteByLastSeenBefore(@Param("threshold") LocalDateTime threshold);
}