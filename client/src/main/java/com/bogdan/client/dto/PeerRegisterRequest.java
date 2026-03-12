package com.bogdan.client.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PeerRegisterRequest {
    private String ip;
    private int port;
    private List<FileInfoDto> files;
}