package com.bogdan.tracker.api.mapper;

import com.bogdan.tracker.api.dto.FileInfoDto;
import com.bogdan.tracker.domain.model.FileInfo;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileInfoMapper {

    public static FileInfo toFileInfo(FileInfoDto dto) {
        return FileInfo.builder()
                .hash(dto.getHash())
                .name(dto.getName())
                .size(dto.getSize())
                .build();
    }

    public static FileInfoDto toFileInfoDto(FileInfo fileInfo) {
        return FileInfoDto.builder()
                .hash(fileInfo.getHash())
                .name(fileInfo.getName())
                .size(fileInfo.getSize())
                .createdTime(fileInfo.getCreatedTime())
                .updatedTime(fileInfo.getUpdatedTime())
                .build();
    }
}
