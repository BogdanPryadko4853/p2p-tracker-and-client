package com.bogdan.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeerResponse {
    private UUID id;
    private String ip;
    private int port;
    private LocalDateTime lastSeen;
    private List<FileInfoDto> files;
}