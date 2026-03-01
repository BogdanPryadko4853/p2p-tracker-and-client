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
public class PeerListResponse {
    private List<PeerResponse> peers;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}