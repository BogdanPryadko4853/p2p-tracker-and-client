package com.bogdan.client.infra;

import com.bogdan.client.dto.FileInfoDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Service
public class FileManager {

    private final String sharedDir;
    private final String downloadDir;

    public FileManager(
            @Value("${p2p.shared-dir:./shared}") String sharedDir,
            @Value("${p2p.download-dir:./downloads}") String downloadDir) {
        this.sharedDir = sharedDir;
        this.downloadDir = downloadDir;

        createDirectories();
        log.info("FileManager initialized with shared dir: {}, download dir: {}", sharedDir, downloadDir);
    }

    private void createDirectories() {
        new File(sharedDir).mkdirs();
        new File(downloadDir).mkdirs();
    }

    public List<FileInfoDto> getSharedFiles() {
        List<FileInfoDto> files = new ArrayList<>();
        File dir = new File(sharedDir);

        File[] fileList = dir.listFiles();
        if (fileList == null) {
            log.warn("Shared directory does not exist or is not a directory: {}", sharedDir);
            return files;
        }

        for (File file : fileList) {
            if (file.isFile()) {
                try {
                    FileInfoDto fileInfo = FileInfoDto.builder()
                            .name(file.getName())
                            .size(file.length())
                            .hash(calculateHash(file))
                            .build();
                    files.add(fileInfo);
                    log.debug("Found shared file: {} ({} bytes)", file.getName(), file.length());
                } catch (Exception e) {
                    log.error("Failed to process file: {}", file.getName(), e);
                }
            }
        }

        log.info("Loaded {} shared files from {}", files.size(), sharedDir);
        return files;
    }

    public File getDownloadFile(String fileName) {
        return Paths.get(downloadDir, fileName).toFile();
    }

    public File getSharedFile(String fileName) {
        return Paths.get(sharedDir, fileName).toFile();
    }

    private String calculateHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        log.debug("Calculated hash for {}: {}", file.getName(), sb.toString());
        return sb.toString();
    }
}