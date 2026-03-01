package com.bogdan.tracker.infrastructure.schedular;

import com.bogdan.tracker.domain.repository.PeerRepository;
import com.bogdan.tracker.infrastructure.scheduler.PeerCleanupScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PeerCleanupSchedulerTest {

    @Mock
    PeerRepository peerRepository;

    @InjectMocks
    PeerCleanupScheduler peerCleanupScheduler;

    @Test
    void deleteInactivePeers_shouldDeletePeers_whenInactivePeersExist(){
        when(peerRepository.deleteByLastSeenBefore(any(LocalDateTime.class))).thenReturn(1);

        peerCleanupScheduler.deleteInactivePeers();

        verify(peerRepository, times(1)).deleteByLastSeenBefore(any(LocalDateTime.class));
        verifyNoMoreInteractions(peerRepository);
    }
}
