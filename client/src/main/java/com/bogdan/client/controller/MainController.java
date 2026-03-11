package com.bogdan.client.controller;

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
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/fxml/main.fxml")
public class MainController {

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

    @FXML
    public void initialize() {
        setupSearchTable();
        setupDownloadsTable();
        setupSharedFilesList();
        setupButtons();
        updateStats();
    }

    private void setupSearchTable() {
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        fileHashColumn.setCellValueFactory(new PropertyValueFactory<>("shortHash"));
        peersCountColumn.setCellValueFactory(new PropertyValueFactory<>("peersCount"));

        searchResultsTable.setPlaceholder(new Label("No files found. Try searching..."));
        searchResultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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
        if (query != null && !query.trim().isEmpty()) {
            statusLabel.setText("Searching for: " + query + "...");
        }
    }

    private void startDownload(FileInfo file) {
        statusLabel.setText("Starting download: " + file.getName());
        // TODO Здесь будет логика скачивания
    }

    private void addSharedFile() {
        // TODO Здесь будет выбор файла для раздачи
    }

    private void removeSharedFile() {
        String selected = sharedFilesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sharedFilesList.getItems().remove(selected);
            updateStats();
        }
    }

    private void openSettings() {
        // TODO Здесь будет открытие окна настроек
    }

    private void updateStats() {
        peersCountLabel.setText("0");
        sharedCountLabel.setText(String.valueOf(sharedFilesList.getItems().size()));
        downloadsCountLabel.setText(String.valueOf(downloadsTable.getItems().size()));

        uploadProgressBar.setProgress(0.0);
        uploadSpeedLabel.setText("0 KB/s");
    }

    public void loadDemoData() {
        ObservableList<FileInfo> demoFiles = FXCollections.observableArrayList(
                new FileInfo("ubuntu-22.04.iso", 3.8, "a1b2c3d4...", 15),
                new FileInfo("movie.mp4", 1.5, "e5f6g7h8...", 8),
                new FileInfo("document.pdf", 2.5, "i9j0k1l2...", 3),
                new FileInfo("music.flac", 0.3, "m3n4o5p6...", 12)
        );
        searchResultsTable.setItems(demoFiles);

        ObservableList<DownloadItem> demoDownloads = FXCollections.observableArrayList(
                new DownloadItem("ubuntu-22.04.iso", "45%", "1.2 MB/s", "Downloading"),
                new DownloadItem("movie.mp4", "100%", "0 KB/s", "Completed")
        );
        downloadsTable.setItems(demoDownloads);

        sharedFilesList.getItems().addAll(
                "linux-mint.iso (2.1 GB)",
                "tutorial.pdf (5.3 MB)"
        );

        updateStats();
    }

    public static class FileInfo {
        private final String name;
        private final double size;
        private final String hash;
        private final int peersCount;

        public FileInfo(String name, double size, String hash, int peersCount) {
            this.name = name;
            this.size = size;
            this.hash = hash;
            this.peersCount = peersCount;
        }

        public String getName() {
            return name;
        }

        public String getFormattedSize() {
            if (size < 1) return (size * 1024) + " MB";
            return size + " GB";
        }

        public String getShortHash() {
            return hash;
        }

        public int getPeersCount() {
            return peersCount;
        }
    }

    public record DownloadItem(String name, String progress, String speed, String status) {
    }
}