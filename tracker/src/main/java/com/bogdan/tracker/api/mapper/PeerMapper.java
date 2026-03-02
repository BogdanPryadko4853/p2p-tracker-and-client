package com.bogdan.tracker.api.mapper;

import com.bogdan.tracker.api.dto.PeerRegisterRequest;
import com.bogdan.tracker.api.dto.PeerResponse;
import com.bogdan.tracker.domain.model.Peer;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class PeerMapper {

    public static Peer toPeer(PeerRegisterRequest registerRequest) {
        return Peer.builder()
                .ip(registerRequest.getIp())
                .port(registerRequest.getPort())
                .lastSeen(LocalDateTime.now())
                .files(registerRequest.getFiles().stream()
                        .map(FileInfoMapper::toFileInfo)
                        .toList())
                .build();
    }

    public static PeerResponse toPeerResponse(Peer peer) {
        return PeerResponse.builder()
                .id(peer.getId())
                .ip(peer.getIp())
                .port(peer.getPort())
                .lastSeen(peer.getLastSeen())
                .files(peer.getFiles().stream()
                        .map(FileInfoMapper::toFileInfoDto)
                        .toList())
                .build();
    }
}