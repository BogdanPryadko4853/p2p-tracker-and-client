package com.bogdan.tracker.domain.repository.jpa;

import com.bogdan.tracker.domain.model.Peer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PeerJpaRepository extends JpaRepository<Peer, UUID> {
}
