package com.bogdan.tracker.common;

import com.bogdan.tracker.domain.model.FileInfo;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class FileInfoUtils {

    public static List<FileInfo> createTwoFiles() {
        return List.of(
                createOneFile("hash1"),
                createOneFile("hash2")
        );
    }

    public static FileInfo createOneFile(String hash) {
        return FileInfo.builder()
                .hash(hash)
                .name("file_" + hash + ".txt")
                .size(1024L)
                .build();
    }

}
