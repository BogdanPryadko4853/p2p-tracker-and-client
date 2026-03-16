package com.bogdan.tracker.api.controller;

import com.bogdan.tracker.api.dto.ActivePeersStatsResponse;
import com.bogdan.tracker.api.dto.FileInfoDto;
import com.bogdan.tracker.api.dto.PeerRegisterRequest;
import com.bogdan.tracker.api.dto.PeerResponse;
import com.bogdan.tracker.api.mapper.FileInfoMapper;
import com.bogdan.tracker.api.mapper.PeerMapper;
import com.bogdan.tracker.domain.model.FileInfo;
import com.bogdan.tracker.domain.model.Peer;
import com.bogdan.tracker.domain.service.FileInfoServiceData;
import com.bogdan.tracker.domain.service.PeerServiceData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/peers")
@Tag(name = "Peer Controller", description = "Управление пирами в P2P сети")
public class PeerController {

    private final PeerServiceData peerService;
    private final FileInfoServiceData fileInfoService;

    @Operation(summary = "Регистрация нового пира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пир успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса", content = @Content),
            @ApiResponse(responseCode = "409", description = "Пир с таким IP и портом уже существует", content = @Content)
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID registerPeer(@RequestBody PeerRegisterRequest request) {
        Peer peer = PeerMapper.toPeer(request);
        return peerService.registerPeer(peer);
    }

    @Operation(summary = "Отправить heartbeat (сигнал жизни)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Heartbeat принят"),
            @ApiResponse(responseCode = "404", description = "Пир не найден", content = @Content)
    })
    @PostMapping("/{peerId}/heartbeat")
    public void heartbeat(
            @Parameter(description = "ID пира", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID peerId) {
        peerService.updateLastSeenPeer(peerId);
    }

    @Operation(summary = "Получить информацию о пире по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пире найдена"),
            @ApiResponse(responseCode = "404", description = "Пир не найден", content = @Content)
    })
    @GetMapping("/{peerId}")
    public PeerResponse getPeer(
            @Parameter(description = "ID пира", required = true)
            @PathVariable UUID peerId) {
        return PeerMapper.toPeerResponse(peerService.findPeerById(peerId));
    }

    @Operation(summary = "Получить список всех пиров")
    @ApiResponse(responseCode = "200", description = "Список пиров получен")
    @GetMapping
    public List<PeerResponse> getAllPeers() {
        return peerService.findAllPeers().stream()
                .map(PeerMapper::toPeerResponse)
                .toList();
    }

    @Operation(summary = "Удалить пира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пир успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Пир не найден", content = @Content)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{peerId}")
    public void deletePeer(
            @Parameter(description = "ID пира", required = true)
            @PathVariable UUID peerId) {
        peerService.deletePeer(peerId);
    }

    @Operation(summary = "Получить список пиров, раздающих файл")
    @ApiResponse(responseCode = "200", description = "Список пиров получен")
    @GetMapping("/files/{fileHash}/peers")
    public List<PeerResponse> getPeersByFileHash(
            @Parameter(description = "Хеш файла", required = true, example = "a1b2c3d4e5f6...")
            @PathVariable String fileHash) {
        return peerService.findPeersByFileHash(fileHash).stream()
                .map(PeerMapper::toPeerResponse)
                .toList();
    }

    @Operation(summary = "Получить список файлов пира")
    @ApiResponse(responseCode = "200", description = "Список файлов получен")
    @GetMapping("/{peerId}/files")
    public List<FileInfoDto> getFilesOfPeer(
            @Parameter(description = "ID пира", required = true)
            @PathVariable UUID peerId) {
        return fileInfoService.findFilesByPeerId(peerId).stream()
                .map(FileInfoMapper::toFileInfoDto)
                .toList();
    }

    @Operation(summary = "Поиск файлов по имени")
    @ApiResponse(responseCode = "200", description = "Результаты поиска")
    @GetMapping("/files/search")
    public List<FileInfoDto> searchFilesByName(
            @Parameter(description = "Поисковый запрос", required = true, example = "photo")
            @RequestParam String query) {
        return fileInfoService.searchFilesByName(query).stream()
                .map(FileInfoMapper::toFileInfoDto)
                .toList();
    }

    @Operation(summary = "Удалить файл у пира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Файл успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Файл не найден", content = @Content)
    })
    @DeleteMapping("/{peerId}/files/{fileHash}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFileFromPeer(
            @PathVariable UUID peerId,
            @PathVariable String fileHash) {
        peerService.removeFileFromPeer(peerId, fileHash);
    }

    @Operation(summary = "Получить статистику активных пиров")
    @ApiResponse(responseCode = "200", description = "Статистика получена")
    @GetMapping("/stats/active")
    public ActivePeersStatsResponse getActivePeersStats() {
        long activeCount = peerService.findActivePeers(LocalDateTime.now().minusMinutes(5)).size();
        return ActivePeersStatsResponse.builder()
                .activeCount(activeCount)
                .period("last 5 minutes")
                .build();
    }

    @PutMapping("/{peerId}/files")
    public void updateFilesOfPeer(
            @PathVariable UUID peerId,
            @RequestBody List<FileInfoDto> files) {

        List<FileInfo> fileInfos = files.stream()
                .map(FileInfoMapper::toFileInfo)
                .collect(Collectors.toList());

        peerService.updatePeerFiles(peerId, fileInfos);
    }

    @Operation(summary = "Добавить файл пиру")
    @ApiResponse(responseCode = "200", description = "Файл добавлен")
    @PostMapping("/{peerId}/files")
    public void addFileToPeer(
            @Parameter(description = "ID пира", required = true)
            @PathVariable UUID peerId,
            @Parameter(description = "Информация о файле", required = true)
            @RequestBody FileInfoDto file) {
        FileInfo fileInfo = FileInfoMapper.toFileInfo(file);
        fileInfoService.saveFile(fileInfo);
    }

    @Operation(summary = "Получить общее количество пиров")
    @ApiResponse(responseCode = "200", description = "Количество пиров")
    @GetMapping("/stats/total")
    public Long getTotalPeersStats() {
        return peerService.findAllPeers().stream().count();
    }
}