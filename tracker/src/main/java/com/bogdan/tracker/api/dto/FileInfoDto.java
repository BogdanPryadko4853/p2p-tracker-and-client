package com.bogdan.tracker.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDto {
    private String hash;
    private String name;
    private long size;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}