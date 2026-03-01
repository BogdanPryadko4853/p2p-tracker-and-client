package com.bogdan.tracker.api.controller;

import com.bogdan.tracker.api.dto.ActivePeersStatsResponse;
import com.bogdan.tracker.api.dto.FileInfoDto;
import com.bogdan.tracker.api.dto.PeerRegisterRequest;
import com.bogdan.tracker.api.dto.PeerResponse;
import com.bogdan.tracker.api.mapper.FileInfoMapper;
import com.bogdan.tracker.api.mapper.PeerMapper;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.service.FileInfoService;
import com.bogdan.tracker.domain.service.PeerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/peers")
public class PeerController {

    private final PeerService peerService;
    private final FileInfoService fileInfoService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public void registerPeer(@RequestBody PeerRegisterRequest request) {
        Peer peer = PeerMapper.toPeer(request);
        peerService.savePeer(peer);
    }

    @PostMapping("/{peerId}/heartbeat")
    public void heartbeat(@PathVariable UUID peerId) {
        peerService.updateLastSeenPeer(peerId);
    }

    @GetMapping("/{peerId}")
    public PeerResponse getPeer(@PathVariable UUID peerId) {
        return PeerMapper.toPeerResponse(peerService.findPeerById(peerId));
    }

    @GetMapping
    public List<PeerResponse> getAllPeers() {
        return peerService.findAllPeers().stream()
                .map(PeerMapper::toPeerResponse)
                .toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{peerId}")
    public void deletePeer(@PathVariable UUID peerId) {
        peerService.deletePeer(peerId);
    }

    @GetMapping("/files/{fileHash}/peers")
    public List<PeerResponse> getPeersByFileHash(@PathVariable String fileHash) {
        return peerService.findPeersByFileHash(fileHash).stream()
                .map(PeerMapper::toPeerResponse)
                .toList();
    }

    @GetMapping("/{peerId}/files")
    public List<FileInfoDto> getFilesOfPeer(@PathVariable UUID peerId) {
        return fileInfoService.findFilesByPeerId(peerId).stream()
                .map(FileInfoMapper::toFileInfoDto)
                .toList();
    }

    @GetMapping("/files/search")
    public List<FileInfoDto> searchFilesByName(@RequestParam String query) {
        return fileInfoService.searchFilesByName(query).stream()
                .map(FileInfoMapper::toFileInfoDto)
                .toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{peerId}/files/{fileHash}")
    public void removeFileFromPeer(@PathVariable UUID peerId, @PathVariable String fileHash) {
        fileInfoService.deleteFile(fileHash);
    }

    @GetMapping("/stats/active")
    public ActivePeersStatsResponse getActivePeersStats() {
        long activeCount = peerService.findActivePeers(LocalDateTime.now().minusMinutes(5)).size();
        return ActivePeersStatsResponse.builder()
                .activeCount(activeCount)
                .period("last 5 minutes")
                .build();
    }

    @PutMapping("/{peerId}/files")
    public void updateFilesOfPeer(@PathVariable UUID peerId, @RequestBody List<FileInfoDto> files) {
        files.forEach(fileInfoDto -> {
            FileInfo fileInfo = FileInfoMapper.toFileInfo(fileInfoDto);
            fileInfoService.saveFile(fileInfo);
        });
    }

    @PostMapping("/{peerId}/files")
    public void addFileToPeer(@PathVariable UUID peerId, @RequestBody FileInfoDto file) {
        FileInfo fileInfo = FileInfoMapper.toFileInfo(file);
        fileInfoService.saveFile(fileInfo);
    }

    @GetMapping("/stats/total")
    public Long getTotalPeersStats() {
        return peerService.findAllPeers().stream().count();
    }
}