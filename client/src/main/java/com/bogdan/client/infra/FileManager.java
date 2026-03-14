package com.bogdan.client.infra;

import com.bogdan.client.common.ClientConfigConstant;
import com.bogdan.client.dto.FileInfoDto;
import com.bogdan.client.util.FileUtils;
import com.bogdan.client.util.HashCalculatorUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Service
public class FileManager {

    private final FileUtils fileUtils;

    private final ClientConfigConstant clientConfigConstant;

    public FileManager(ClientConfigConstant clientConfigConstant, FileUtils fileUtils) {
        this.clientConfigConstant = clientConfigConstant;
        this.fileUtils = fileUtils;
        this.fileUtils.createDirectories();
    }

    public List<FileInfoDto> getSharedFiles() {
        List<FileInfoDto> files = new ArrayList<>();
        File dir = new File(clientConfigConstant.SHARED_DIR);

        File[] fileList = dir.listFiles();
        if (fileList == null) {
            log.warn("Shared directory does not exist or is not a directory: {}", clientConfigConstant.SHARED_DIR);
            return files;
        }

        for (File file : fileList) {
            if (file.isFile()) {
                try {
                    FileInfoDto fileInfo = FileInfoDto.builder()
                            .name(file.getName())
                            .size(file.length())
                            .hash(HashCalculatorUtils.calculateHash(file))
                            .build();
                    files.add(fileInfo);
                    log.debug("Found shared file: {} ({} bytes)", file.getName(), file.length());
                } catch (Exception e) {
                    log.error("Failed to process file: {}", file.getName(), e);
                }
            }
        }
        log.info("Loaded {} shared files from {}", files.size(), clientConfigConstant.SHARED_DIR);
        return files;
    }
}