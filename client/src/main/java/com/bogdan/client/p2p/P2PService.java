package com.bogdan.client.p2p;

import com.bogdan.client.dto.FileInfoDto;
import com.bogdan.client.dto.PeerResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
public class P2PService {

    private int port;
    private Consumer<DownloadTask> onDownloadUpdate;

    @Value("${p2p.port:6881}")
    public void setPort(int port) {
        this.port = port;
    }

    public void setOnDownloadUpdate(Consumer<DownloadTask> callback) {
        this.onDownloadUpdate = callback;
    }

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Map<String, DownloadTask> downloads = new ConcurrentHashMap<>();
    private final Map<String, FileInfoDto> sharedFiles = new ConcurrentHashMap<>();

    public void startServer() {
        executorService = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            log.info("P2P server started on port {}", port);

            executorService.submit(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executorService.submit(new ClientHandler(clientSocket));
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            log.error("Error accepting connection: {}", e.getMessage());
                        }
                    }
                }
            });
        } catch (IOException e) {
            log.error("Failed to start P2P server: {}", e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                String command = in.readUTF();

                if (command.startsWith("GET")) {
                    String[] parts = command.split(" ");
                    String fileHash = parts[1];
                    long offset = Long.parseLong(parts[2]);
                    int length = Integer.parseInt(parts[3]);

                    handleFileRequest(fileHash, offset, length, out);
                }

            } catch (IOException e) {
                log.error("Error handling client: {}", e.getMessage());
            }
        }

        private void handleFileRequest(String fileHash, long offset, int length, DataOutputStream out)
                throws IOException {
            FileInfoDto fileInfo = sharedFiles.get(fileHash);
            if (fileInfo == null) {
                out.writeUTF("ERROR File not found");
                return;
            }

            File file = new File("shared/" + fileInfo.getName());
            if (!file.exists()) {
                out.writeUTF("ERROR File not available");
                return;
            }

            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(offset);
                byte[] buffer = new byte[length];
                int bytesRead = raf.read(buffer);

                out.writeUTF("DATA " + bytesRead);
                out.write(buffer, 0, bytesRead);
                log.debug("Sent {} bytes of file {} at offset {}", bytesRead, fileInfo.getName(), offset);
            }
        }
    }

    public void downloadFile(FileInfoDto fileInfo, PeerResponse peer, String fileName) {
        String downloadId = fileInfo.getHash() + "_" + System.currentTimeMillis();
        DownloadTask task = new DownloadTask(downloadId, fileInfo, peer, fileName);
        downloads.put(downloadId, task);
        executorService.submit(task);
    }

    @Getter
    public class DownloadTask implements Runnable {
        private final String id;
        private final FileInfoDto fileInfo;
        private final PeerResponse peer;
        private final String fileName;
        private volatile int progress;
        private volatile String status;
        private volatile String downloadPath;

        public DownloadTask(String id, FileInfoDto fileInfo, PeerResponse peer, String fileName) {
            this.id = id;
            this.fileInfo = fileInfo;
            this.peer = peer;
            this.fileName = fileName;
            this.status = "Starting";
            this.progress = 0;
        }

        @Override
        public void run() {
            status = "Downloading";
            downloadPath = "downloads/" + fileName;
            File file = new File(downloadPath);
            file.getParentFile().mkdirs();

            try (Socket socket = new Socket(peer.getIp(), peer.getPort());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

                long fileSize = fileInfo.getSize();
                int chunkSize = 64 * 1024;

                for (long offset = 0; offset < fileSize; offset += chunkSize) {
                    int length = (int) Math.min(chunkSize, fileSize - offset);

                    out.writeUTF("GET " + fileInfo.getHash() + " " + offset + " " + length);

                    String response = in.readUTF();
                    if (response.startsWith("DATA")) {
                        int dataLength = Integer.parseInt(response.split(" ")[1]);
                        byte[] buffer = new byte[dataLength];
                        in.readFully(buffer);

                        raf.seek(offset);
                        raf.write(buffer);

                        progress = (int) ((offset + dataLength) * 100 / fileSize);
                        log.debug("Download progress for {}: {}%", fileName, progress);

                        if (onDownloadUpdate != null) {
                            onDownloadUpdate.accept(this);
                        }
                    }
                }

                status = "Completed";
                log.info("Download completed: {} saved to {}", fileName,
                        Paths.get(downloadPath).toAbsolutePath().normalize());

                if (onDownloadUpdate != null) {
                    onDownloadUpdate.accept(this);
                }

            } catch (IOException e) {
                status = "Failed";
                log.error("Download failed for {}: {}", fileName, e.getMessage());

                if (onDownloadUpdate != null) {
                    onDownloadUpdate.accept(this);
                }
            }
        }
    }

    public void addSharedFile(FileInfoDto fileInfo) {
        sharedFiles.put(fileInfo.getHash(), fileInfo);
    }

    public void removeSharedFile(String hash) {
        sharedFiles.remove(hash);
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (IOException e) {
            log.error("Error stopping P2P service: {}", e.getMessage());
        }
    }
}