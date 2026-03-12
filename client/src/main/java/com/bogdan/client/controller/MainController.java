package com.bogdan.client.controller;

import com.bogdan.client.dto.FileInfoDto;
import com.bogdan.client.dto.PeerResponse;
import com.bogdan.client.infra.FileManager;
import com.bogdan.client.p2p.P2PService;
import com.bogdan.client.tracker.TrackerService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@FxmlView("/fxml/main.fxml")
public class MainController {

    private final TrackerService trackerService;
    private final P2PService p2pService;
    private final FileManager fileManager;
    private final Map<String, DownloadItem> downloadItemMap = new ConcurrentHashMap<>();

    @Value("${p2p.port:6881}")
    private int p2pPort;

    @Value("${tracker.url:http://localhost:8000}")
    private String trackerUrl;

    public MainController(TrackerService trackerService,
                          P2PService p2pService,
                          FileManager fileManager) {
        this.trackerService = trackerService;
        this.p2pService = p2pService;
        this.fileManager = fileManager;
    }

    @FXML
    private TabPane mainTabPane;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<FileInfo> searchResultsTable;
    @FXML
    private TableColumn<FileInfo, String> fileNameColumn;
    @FXML
    private TableColumn<FileInfo, String> fileSizeColumn;
    @FXML
    private TableColumn<FileInfo, String> fileHashColumn;
    @FXML
    private TableColumn<FileInfo, Integer> peersCountColumn;
    @FXML
    private TableView<DownloadItem> downloadsTable;
    @FXML
    private TableColumn<DownloadItem, String> downloadNameColumn;
    @FXML
    private TableColumn<DownloadItem, String> downloadProgressColumn;
    @FXML
    private TableColumn<DownloadItem, String> downloadSpeedColumn;
    @FXML
    private TableColumn<DownloadItem, String> downloadStatusColumn;
    @FXML
    private ListView<String> sharedFilesList;
    @FXML
    private Label statusLabel;
    @FXML
    private Label peersCountLabel;
    @FXML
    private Label sharedCountLabel;
    @FXML
    private Label downloadsCountLabel;
    @FXML
    private Button addShareButton;
    @FXML
    private Button removeShareButton;
    @FXML
    private Button settingsButton;
    @FXML
    private ProgressBar uploadProgressBar;
    @FXML
    private Label uploadSpeedLabel;

    private final ObservableList<FileInfo> searchResults = FXCollections.observableArrayList();
    private final ObservableList<DownloadItem> downloads = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Initializing MainController with P2P port: {}, Tracker URL: {}", p2pPort, trackerUrl);

        setupSearchTable();
        setupDownloadsTable();
        setupSharedFilesList();
        setupButtons();
        updateStats();

        p2pService.startServer();

        p2pService.setOnDownloadUpdate(task -> {
            Platform.runLater(() -> {
                DownloadItem item = downloadItemMap.get(task.getId());
                if (item != null) {
                    item.setProgress(task.getProgress() + "%");
                    item.setStatus(task.getStatus());

                    if ("Completed".equals(task.getStatus())) {
                        String path = Paths.get(task.getDownloadPath()).toAbsolutePath().normalize().toString();
                        item.setStatus("Completed - saved to: " + path);
                        log.info("Download completed and saved to: {}", path);
                    }

                    downloadsTable.refresh();
                    updateStats();
                }
            });
        });

        try {
            trackerService.register("127.0.0.1", p2pPort, fileManager.getSharedFiles());
            log.info("Successfully registered with tracker");
            statusLabel.setText("Connected to tracker");
        } catch (Exception e) {
            log.error("Failed to register with tracker: {}", e.getMessage());
            statusLabel.setText("Failed to connect to tracker");
        }

        refreshSharedFiles();

        downloadsTable.setItems(downloads);
    }

    private void setupSearchTable() {
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        fileHashColumn.setCellValueFactory(new PropertyValueFactory<>("shortHash"));
        peersCountColumn.setCellValueFactory(new PropertyValueFactory<>("peersCount"));

        searchResultsTable.setPlaceholder(new Label("No files found. Try searching..."));
        searchResultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        searchResultsTable.setItems(searchResults);

        searchResultsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileInfo selected = searchResultsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    startDownload(selected);
                }
            }
        });
    }

    private void setupDownloadsTable() {
        downloadNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        downloadProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        downloadSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        downloadStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        downloadsTable.setPlaceholder(new Label("No active downloads"));
        downloadsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupSharedFilesList() {
        sharedFilesList.setPlaceholder(new Label("No shared files"));
        sharedFilesList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("System", FontWeight.NORMAL, 13));
                }
            }
        });
    }

    private void setupButtons() {
        searchButton.setOnAction(event -> performSearch());
        addShareButton.setOnAction(event -> addSharedFile());
        removeShareButton.setOnAction(event -> removeSharedFile());
        settingsButton.setOnAction(event -> openSettings());
        searchField.setOnAction(event -> performSearch());
    }

    @FXML
    private void performSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            statusLabel.setText("Please enter a search query");
            return;
        }

        statusLabel.setText("Searching for: " + query + "...");

        try {
            List<FileInfoDto> dtos = trackerService.searchFiles(query.trim());
            searchResults.clear();

            for (FileInfoDto dto : dtos) {
                int peersCount = trackerService.getPeersForFile(dto.getHash()).size();
                FileInfo info = new FileInfo(dto.getName(), dto.getSize(), dto.getHash(), peersCount);
                searchResults.add(info);
            }

            statusLabel.setText("Found " + searchResults.size() + " files");
            log.info("Search completed, found {} files", searchResults.size());

        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage());
            statusLabel.setText("Search failed: " + e.getMessage());
        }
    }

    private void startDownload(FileInfo file) {
        try {
            List<PeerResponse> peers = trackerService.getPeersForFile(file.getHash());
            if (peers.isEmpty()) {
                statusLabel.setText("No peers available for this file");
                return;
            }

            PeerResponse peer = peers.get(0);
            FileInfoDto dto = FileInfoDto.builder()
                    .hash(file.getHash())
                    .name(file.getName())
                    .size(file.getSizeInBytes())
                    .build();

            p2pService.downloadFile(dto, peer, file.getName());

            DownloadItem item = new DownloadItem(
                    file.getName(),
                    "0%",
                    "0 KB/s",
                    "Starting"
            );

            downloadItemMap.put(dto.getHash() + "_" + System.currentTimeMillis(), item);
            downloads.add(item);

            statusLabel.setText("Download started: " + file.getName());
            log.info("Started download of {} from {}:{}", file.getName(), peer.getIp(), peer.getPort());

        } catch (Exception e) {
            log.error("Failed to start download: {}", e.getMessage());
            statusLabel.setText("Download failed: " + e.getMessage());
        }
    }

    private void addSharedFile() {
        statusLabel.setText("Add shared file - not implemented yet");
    }

    private void removeSharedFile() {
        String selected = sharedFilesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sharedFilesList.getItems().remove(selected);
            updateStats();
            statusLabel.setText("File removed from shares");
        }
    }

    private void openSettings() {
        statusLabel.setText("Settings - not implemented yet");
    }

    private void updateStats() {
        sharedCountLabel.setText(String.valueOf(sharedFilesList.getItems().size()));
        downloadsCountLabel.setText(String.valueOf(downloads.size()));
        uploadSpeedLabel.setText("0 KB/s");
        uploadProgressBar.setProgress(0.0);
    }

    private void refreshSharedFiles() {
        List<FileInfoDto> shared = fileManager.getSharedFiles();
        sharedFilesList.getItems().clear();

        for (FileInfoDto dto : shared) {
            p2pService.addSharedFile(dto);
            String display = dto.getName() + " (" + formatSize(dto.getSize()) + ")";
            sharedFilesList.getItems().add(display);
        }

        updateStats();
        log.info("Loaded {} shared files", shared.size());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static class FileInfo {
        @Getter
        private final String name;
        private final long size;
        @Getter
        private final String hash;
        @Getter
        private final int peersCount;

        public FileInfo(String name, long size, String hash, int peersCount) {
            this.name = name;
            this.size = size;
            this.hash = hash;
            this.peersCount = peersCount;
        }

        public String getFormattedSize() {
            return formatSize(size);
        }

        public String getShortHash() {
            return hash.length() > 8 ? hash.substring(0, 8) + "..." : hash;
        }

        public long getSizeInBytes() {
            return size;
        }

        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            String pre = "KMGTPE".charAt(exp - 1) + "";
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
    }

    @Getter
    public static class DownloadItem {
        private final String name;
        @Setter
        private String progress;
        @Setter
        private String speed;
        @Setter
        private String status;

        public DownloadItem(String name, String progress, String speed, String status) {
            this.name = name;
            this.progress = progress;
            this.speed = speed;
            this.status = status;
        }

    }
}