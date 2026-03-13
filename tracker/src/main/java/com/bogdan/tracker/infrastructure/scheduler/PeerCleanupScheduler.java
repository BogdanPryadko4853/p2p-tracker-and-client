package com.bogdan.tracker.infrastructure.scheduler;

import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.service.FileInfoServiceData;
import com.bogdan.tracker.infrastructure.repository.jpa.PeerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeerCleanupScheduler {

    private final PeerJpaRepository peerRepository;
    private final FileInfoServiceData fileInfoService;

    @Transactional
    @Scheduled(fixedDelay = 120000)
    public void deleteInactivePeers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        int deletedCount = peerRepository.deleteByLastSeenBefore(threshold);

        if (deletedCount > 0) {
            log.info("Cleaned up {} inactive peers (last seen before {})", deletedCount, threshold);
        } else {
            log.debug("No inactive peers to clean up");
        }
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupOrphanedFiles() {
        List<FileInfo> allFiles = fileInfoService.findAllFiles();

        for (FileInfo file : allFiles) {
            List<Peer> peersWithFile = peerRepository.findPeersByFileHash(file.getHash());
            if (peersWithFile.isEmpty()) {
                fileInfoService.deleteFile(file.getHash());
                log.info("Deleted orphaned file {} from DB", file.getHash());
            }
        }
    }
}