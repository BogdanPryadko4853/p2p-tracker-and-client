package com.bogdan.tracker.api;

import com.bogdan.tracker.api.controller.PeerController;
import com.bogdan.tracker.api.dto.FileInfoDto;
import com.bogdan.tracker.api.dto.PeerRegisterRequest;
import com.bogdan.tracker.api.handler.GlobalExceptionHandler;
import com.bogdan.tracker.domain.exception.Peer.PeerAlreadyExistsException;
import com.bogdan.tracker.domain.exception.Peer.PeerNotFoundException;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.service.FileInfoService;
import com.bogdan.tracker.domain.service.PeerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PeerController.class)
@Import(GlobalExceptionHandler.class)
public class PeerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PeerService peerService;

    @MockitoBean
    private FileInfoService fileInfoService;

    @Test
    void registerPeer_shouldReturnCreated() throws Exception {
        PeerRegisterRequest request = createPeerRegisterRequest();

        mockMvc.perform(post("/api/peers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(peerService, times(1)).savePeer(any(Peer.class));
    }

    @Test
    void registerPeer_shouldReturnConflict_whenPeerExists() throws Exception {
        PeerRegisterRequest request = createPeerRegisterRequest();

        doThrow(new PeerAlreadyExistsException("Peer already exists"))
                .when(peerService).savePeer(any(Peer.class));

        mockMvc.perform(post("/api/peers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Peer already exists"));

        verify(peerService, times(1)).savePeer(any(Peer.class));
    }

    @Test
    void heartbeat_shouldReturnOk() throws Exception {
        UUID peerId = createPeerId();

        mockMvc.perform(post("/api/peers/{peerId}/heartbeat", peerId))
                .andExpect(status().isOk());

        verify(peerService, times(1)).updateLastSeenPeer(peerId);
    }

    @Test
    void getPeer_shouldReturnPeer() throws Exception {
        UUID peerId = createPeerId();
        Peer peer = createPeerWithId(peerId);

        when(peerService.findPeerById(peerId)).thenReturn(peer);

        mockMvc.perform(get("/api/peers/{peerId}", peerId))
                .andDo(System.out::println)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(peerId.toString()))
                .andExpect(jsonPath("$.ip").value(peer.getIp()))
                .andExpect(jsonPath("$.port").value(peer.getPort()));

        verify(peerService, times(1)).findPeerById(peerId);
    }

    @Test
    void getPeer_shouldReturnNotFound_whenPeerDoesNotExist() throws Exception {
        UUID peerId = createPeerId();

        when(peerService.findPeerById(any(UUID.class))).thenThrow(new PeerNotFoundException("Peer not found"));

        mockMvc.perform(get("/api/peers/{peerId}", peerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Peer not found"));
    }

    @Test
    void getAllPeers_shouldReturnList() throws Exception {
        List<Peer> peers = createPeerList();

        when(peerService.findAllPeers()).thenReturn(peers);

        mockMvc.perform(get("/api/peers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ip").value("127.0.0.1"))
                .andExpect(jsonPath("$[1].ip").value("127.0.0.2"));

        verify(peerService, times(1)).findAllPeers();
    }

    @Test
    void deletePeer_shouldReturnNoContent() throws Exception {
        UUID peerId = createPeerId();

        mockMvc.perform(delete("/api/peers/{peerId}", peerId))
                .andExpect(status().isNoContent());

        verify(peerService, times(1)).deletePeer(peerId);
    }

    @Test
    void deletePeer_shouldReturnNotFound_whenPeerDoesNotExist() throws Exception {
        UUID peerId = createPeerId();

        doThrow(new PeerNotFoundException("Peer not found")).when(peerService).deletePeer(peerId);

        mockMvc.perform(delete("/api/peers/{peerId}", peerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Peer not found"));

        verify(peerService, times(1)).deletePeer(peerId);
    }

    @Test
    void getPeersByFileHash_shouldReturnList() throws Exception {
        String fileHash = "hash123";
        List<Peer> peers = createPeerList();

        when(peerService.findPeersByFileHash(fileHash)).thenReturn(peers);

        mockMvc.perform(get("/api/peers/files/{fileHash}/peers", fileHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ip").value("127.0.0.1"))
                .andExpect(jsonPath("$[1].ip").value("127.0.0.2"));

        verify(peerService, times(1)).findPeersByFileHash(fileHash);
    }

    @Test
    void getFilesOfPeer_shouldReturnList() throws Exception {
        UUID peerId = createPeerId();
        List<FileInfo> files = createFileInfoList();

        when(fileInfoService.findFilesByPeerId(peerId)).thenReturn(files);

        mockMvc.perform(get("/api/peers/{peerId}/files", peerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].hash").value("hash1"))
                .andExpect(jsonPath("$[1].hash").value("hash2"));

        verify(fileInfoService, times(1)).findFilesByPeerId(peerId);
    }

    @Test
    void searchFilesByName_shouldReturnList() throws Exception {
        String query = "photo";
        List<FileInfo> files = createFileInfoList();

        when(fileInfoService.searchFilesByName(query)).thenReturn(files);

        mockMvc.perform(get("/api/peers/files/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("file1.txt"))
                .andExpect(jsonPath("$[1].name").value("file2.txt"));

        verify(fileInfoService, times(1)).searchFilesByName(query);
    }

    @Test
    void removeFileFromPeer_shouldReturnNoContent() throws Exception {
        UUID peerId = createPeerId();
        String fileHash = "hash123";

        mockMvc.perform(delete("/api/peers/{peerId}/files/{fileHash}", peerId, fileHash))
                .andExpect(status().isNoContent());

        verify(fileInfoService, times(1)).deleteFile(fileHash);
    }

    @Test
    void getActivePeersStats_shouldReturnStats() throws Exception {
        List<Peer> activePeers = createPeerList();

        when(peerService.findActivePeers(any(LocalDateTime.class))).thenReturn(activePeers);

        mockMvc.perform(get("/api/peers/stats/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCount").value(2))
                .andExpect(jsonPath("$.period").value("last 5 minutes"));

        verify(peerService, times(1)).findActivePeers(any(LocalDateTime.class));
    }

    @Test
    void getTotalPeersStats_shouldReturnCount() throws Exception {
        List<Peer> allPeers = createPeerList();

        when(peerService.findAllPeers()).thenReturn(allPeers);

        mockMvc.perform(get("/api/peers/stats/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        verify(peerService, times(1)).findAllPeers();
    }

    @Test
    void updateFilesOfPeer_shouldReturnOk() throws Exception {
        UUID peerId = createPeerId();
        List<FileInfoDto> files = createFileInfoDtoList();

        mockMvc.perform(put("/api/peers/{peerId}/files", peerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(files)))
                .andExpect(status().isOk());

        verify(fileInfoService, times(2)).saveFile(any(FileInfo.class));
    }

    @Test
    void addFileToPeer_shouldReturnOk() throws Exception {
        UUID peerId = createPeerId();
        FileInfoDto file = createFileInfoDto();

        mockMvc.perform(post("/api/peers/{peerId}/files", peerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(file)))
                .andExpect(status().isOk());

        verify(fileInfoService, times(1)).saveFile(any(FileInfo.class));
    }

    private PeerRegisterRequest createPeerRegisterRequest() {
        return PeerRegisterRequest.builder()
                .ip("127.0.0.1")
                .port(8080)
                .files(List.of())
                .build();
    }

    private UUID createPeerId() {
        return UUID.randomUUID();
    }

    private Peer createPeerWithId(UUID peerId) {
        return Peer.builder()
                .id(peerId)
                .ip("127.0.0.1")
                .port(8080)
                .lastSeen(LocalDateTime.now())
                .files(List.of())
                .build();
    }

    private List<Peer> createPeerList() {
        return List.of(
                Peer.builder()
                        .id(UUID.randomUUID())
                        .ip("127.0.0.1")
                        .port(8080)
                        .lastSeen(LocalDateTime.now())
                        .files(List.of())
                        .build(),
                Peer.builder()
                        .id(UUID.randomUUID())
                        .ip("127.0.0.2")
                        .port(8081)
                        .lastSeen(LocalDateTime.now())
                        .files(List.of())
                        .build()
        );
    }

    private List<FileInfo> createFileInfoList() {
        return List.of(
                FileInfo.builder()
                        .hash("hash1")
                        .name("file1.txt")
                        .size(1024L)
                        .build(),
                FileInfo.builder()
                        .hash("hash2")
                        .name("file2.txt")
                        .size(2048L)
                        .build()
        );
    }

    private FileInfoDto createFileInfoDto() {
        return FileInfoDto.builder()
                .hash("hash1")
                .name("file1.txt")
                .size(1024L)
                .build();
    }

    private List<FileInfoDto> createFileInfoDtoList() {
        return List.of(
                FileInfoDto.builder()
                        .hash("hash1")
                        .name("file1.txt")
                        .size(1024L)
                        .build(),
                FileInfoDto.builder()
                        .hash("hash2")
                        .name("file2.txt")
                        .size(2048L)
                        .build()
        );
    }
}