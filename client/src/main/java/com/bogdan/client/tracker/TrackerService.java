package com.bogdan.client.tracker;

import com.bogdan.client.dto.FileInfoDto;
import com.bogdan.client.dto.PeerRegisterRequest;
import com.bogdan.client.dto.PeerResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TrackerService {

    @Value("${tracker.url:http://localhost:8000}")
    private String trackerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    @Getter
    private UUID peerId;
    private ScheduledExecutorService heartbeatScheduler;

    public void register(String ip, int port, List<FileInfoDto> sharedFiles) {
        try {
            PeerRegisterRequest request = PeerRegisterRequest.builder()
                    .ip(ip)
                    .port(port)
                    .files(sharedFiles)
                    .build();

            String url = trackerUrl + "/api/peers/register";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PeerRegisterRequest> entity = new HttpEntity<>(request, headers);

            log.info("Registering with tracker at {} with {} shared files", url, sharedFiles.size());

            ResponseEntity<UUID> peerFrom = restTemplate.postForEntity(url, entity, UUID.class);

            if (peerFrom.getBody() != null) {
                this.peerId = peerFrom.getBody();
                log.info("Registered with tracker. Peer ID: {}", peerId);

                for (FileInfoDto file : sharedFiles) {
                    log.info("Shared file: {} ({} bytes) - hash: {}",
                            file.getName(), file.getSize(), file.getHash());
                }

                startHeartbeat();
            }
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Peer already registered with this IP and port");
        } catch (Exception e) {
            log.error("Failed to register with tracker: {}", e.getMessage());
        }
    }

    public List<FileInfoDto> searchFiles(String query) {
        try {
            String url = trackerUrl + "/api/peers/files/search?query=" + query;

            ResponseEntity<String> rawResponse = restTemplate.getForEntity(url, String.class);
            log.info("Raw response from tracker: {}", rawResponse.getBody());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse.getBody());

            if (root.isArray()) {
                FileInfoDto[] files = mapper.treeToValue(root, FileInfoDto[].class);
                List<FileInfoDto> result = Arrays.asList(files);
                log.info("Found {} files for query '{}'", result.size(), query);
                return result;
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void updateSharedFiles(List<FileInfoDto> sharedFiles) {
        try {
            String url = trackerUrl + "/api/peers/" + peerId + "/files";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<FileInfoDto>> entity = new HttpEntity<>(sharedFiles, headers);
            restTemplate.put(url, entity);
            log.info("Updated shared files on tracker, count: {}", sharedFiles.size());
        } catch (Exception e) {
            log.error("Failed to update shared files: {}", e.getMessage());
        }
    }

    public List<PeerResponse> getPeersForFile(String fileHash) {
        try {
            String url = trackerUrl + "/api/peers/files/" + fileHash + "/peers";
            log.debug("Getting peers for file hash: {}", fileHash);

            ResponseEntity<PeerResponse[]> response = restTemplate.getForEntity(url, PeerResponse[].class);
            List<PeerResponse> peers = Arrays.asList(Objects.requireNonNull(response.getBody()));

            log.info("Found {} peers for file hash {}", peers.size(), fileHash);
            return peers;

        } catch (Exception e) {
            log.error("Failed to get peers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void startHeartbeat() {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                String url = trackerUrl + "/api/peers/" + peerId + "/heartbeat";
                restTemplate.postForEntity(url, null, Void.class);
                log.debug("Heartbeat sent for peer {}", peerId);
            } catch (Exception e) {
                log.error("Failed to send heartbeat: {}", e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
        }
    }

}