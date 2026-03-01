package com.bogdan.tracker.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerRegisterRequest {
    private String ip;
    private int port;
    private List<FileInfoDto> files;
}