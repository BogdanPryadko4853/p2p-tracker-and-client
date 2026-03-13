package com.bogdan.client.util;

import com.bogdan.client.common.ClientConfigConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class FileUtils {

    private final ClientConfigConstant config;

    public File getDownloadFile(String fileName) {
        return Paths.get(config.getDOWNLOAD_DIR(), fileName).toFile();
    }

    public File getSharedFile(String fileName) {
        return Paths.get(config.getSHARED_DIR(), fileName).toFile();
    }

    public void createDirectories() {
        new File(config.getSHARED_DIR()).mkdirs();
        new File(config.getDOWNLOAD_DIR()).mkdirs();
    }
}