package com.bogdan.tracker.infrastructure.scheduler;

import com.bogdan.tracker.infrastructure.repository.jpa.PeerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeerCleanupScheduler {

    private final PeerJpaRepository peerRepository;

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void deleteInactivePeers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        int deletedCount = peerRepository.deleteByLastSeenBefore(threshold);

        if (deletedCount > 0) {
            log.info("Cleaned up {} inactive peers (last seen before {})", deletedCount, threshold);
        } else {
            log.debug("No inactive peers to clean up");
        }
    }
}