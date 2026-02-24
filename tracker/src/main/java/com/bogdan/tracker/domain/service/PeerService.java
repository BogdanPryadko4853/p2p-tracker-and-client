package com.bogdan.tracker.domain.service;

import com.bogdan.tracker.domain.model.Peer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PeerService {

    void savePeer(Peer peer);

    void deletePeer(UUID peerId);

    void deletePeer(Peer peer);

    List<Peer> findAllPeers();

    Peer findPeerById(UUID id);

    Peer updateLastSeenPeer(UUID id);

    Peer findPeerByIpAndPort(String ip, int port);

    List<Peer> findActivePeers(LocalDateTime since);

    List<Peer> findPeersByFileHash(String fileHash);

    void cleanupInactivePeers(LocalDateTime threshold);
}