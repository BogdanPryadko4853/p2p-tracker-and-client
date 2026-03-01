package com.bogdan.tracker.common;

import com.bogdan.tracker.domain.model.Peer;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class PeerUtils {

    public static List<Peer> createTwoPeers() {
        List<Peer> peers = new ArrayList<>();
        peers.add(createOnePeer());
        peers.add(Peer.builder()
                .id(UUID.randomUUID())
                .port(5678)
                .ip("127.0.0.2")
                .lastSeen(LocalDateTime.now())
                .build());
        return peers;
    }

    public static Peer createOnePeer() {
        return Peer.builder()
                .id(UUID.randomUUID())
                .port(1234)
                .ip("127.0.0.1")
                .lastSeen(LocalDateTime.now())
                .build();
    }
}
