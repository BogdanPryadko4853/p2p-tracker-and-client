package com.bogdan.client.p2p;

import com.bogdan.client.dto.FileInfoDto;
import com.bogdan.client.dto.PeerResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
public class P2PService {

    @Setter
    private Consumer<DownloadTask> onDownloadUpdate;

    @Value("${p2p.port:6881}")
    private int port;

    @Value("${p2p.download-dir:./downloads}")
    private String downloadDir;

    @Value("${p2p.shared-dir:./shared}")
    private String sharedDir;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Map<String, DownloadTask> downloads = new ConcurrentHashMap<>();
    private final Map<String, FileInfoDto> sharedFiles = new ConcurrentHashMap<>();

    public void startServer() {
        executorService = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            log.info("P2P server started on port {}", port);

            executorService.submit(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        clientSocket.setSoTimeout(30000);
                        clientSocket.setKeepAlive(true);
                        executorService.submit(new ClientHandler(clientSocket));
                    } catch (SocketTimeoutException ignored) {

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
            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            log.debug("Handling client: {}", clientInfo);

            try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

                while (true) {
                    String command;
                    try {
                        command = in.readUTF();
                    } catch (EOFException e) {
                        log.debug("Client {} closed connection", clientInfo);
                        break;
                    }

                    if (command == null) break;

                    if (command.startsWith("GET")) {
                        String[] parts = command.split(" ");
                        if (parts.length != 4) {
                            out.writeUTF("ERROR Invalid command");
                            out.flush();
                            continue;
                        }
                        String fileHash = parts[1];
                        long offset = Long.parseLong(parts[2]);
                        int length = Integer.parseInt(parts[3]);
                        handleFileRequest(fileHash, offset, length, out);
                        out.flush();
                    } else {
                        log.warn("Unknown command: {}", command);
                    }
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    log.error("Error handling client {}: {}", clientInfo, e.getMessage());
                }
            }
        }

        private void handleFileRequest(String fileHash, long offset, int length, DataOutputStream out) throws IOException {
            FileInfoDto fileInfo = sharedFiles.get(fileHash);
            if (fileInfo == null) {
                out.writeUTF("ERROR File not found");
                return;
            }

            File file = new File(sharedDir, fileInfo.getName());
            if (!file.exists()) {
                out.writeUTF("ERROR File not available");
                return;
            }

            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                long fileSize = file.length();
                if (offset > fileSize) {
                    out.writeUTF("ERROR Offset beyond file size");
                    return;
                }

                int bytesToRead = (int) Math.min(length, fileSize - offset);
                byte[] buffer = new byte[bytesToRead];
                raf.seek(offset);
                int bytesRead = raf.read(buffer);

                if (bytesRead > 0) {
                    out.writeUTF("DATA " + bytesRead);
                    out.write(buffer, 0, bytesRead);
                    log.debug("Sent {} bytes of file {} at offset {}", bytesRead, fileInfo.getName(), offset);
                } else {
                    out.writeUTF("ERROR No data read");
                }
            }
        }
    }

    public String downloadFile(FileInfoDto fileInfo, List<PeerResponse> peers, String fileName) {
        String downloadId = fileInfo.getHash() + "_" + System.currentTimeMillis();
        DownloadTask task = new DownloadTask(downloadId, fileInfo, peers, fileName);
        downloads.put(downloadId, task);
        executorService.submit(task);
        return downloadId;
    }

    @Getter
    public class DownloadTask implements Runnable {
        private final String id;
        private final FileInfoDto fileInfo;
        private final List<PeerResponse> peers;
        private final String fileName;
        private volatile int progress;
        private volatile String status;
        private volatile String downloadPath;
        private volatile long downloadedBytes;

        public DownloadTask(String id, FileInfoDto fileInfo, List<PeerResponse> peers, String fileName) {
            this.id = id;
            this.fileInfo = fileInfo;
            this.peers = peers;
            this.fileName = fileName;
            this.status = "Starting";
            this.progress = 0;
            this.downloadedBytes = 0;
        }

        @Override
        public void run() {
            downloadPath = Paths.get(downloadDir, fileName).toString();
            File file = new File(downloadPath);
            file.getParentFile().mkdirs();

            downloadedBytes = file.exists() ? file.length() : 0;
            if (downloadedBytes >= fileInfo.getSize()) {
                status = "Completed";
                log.info("File already fully downloaded: {}", fileName);
                if (onDownloadUpdate != null) onDownloadUpdate.accept(this);
                return;
            }

            status = "Downloading";
            log.info("Starting download of {} from {} possible peers, already have {} bytes", fileName, peers.size(), downloadedBytes);

            for (PeerResponse peer : peers) {
                if (downloadedBytes >= fileInfo.getSize()) {
                    break;
                }

                log.info("Trying peer {}:{}", peer.getIp(), peer.getPort());
                try {
                    downloadFromPeer(peer, file);
                } catch (Exception e) {
                    log.warn("Download from peer {} failed: {}", peer.getIp(), e.getMessage());
                }
            }

            if (downloadedBytes >= fileInfo.getSize()) {
                status = "Completed";
                log.info("Download completed: {} saved to {}", fileName, Paths.get(downloadPath).toAbsolutePath());
            } else {
                status = "Paused";
                log.info("Download paused at {} bytes, no more peers", downloadedBytes);
            }

            if (onDownloadUpdate != null) onDownloadUpdate.accept(this);
        }

        private void downloadFromPeer(PeerResponse peer, File file) throws IOException {
            Socket socket = new Socket(peer.getIp(), peer.getPort());
            try {
                socket.setSoTimeout(30000);
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    raf.seek(downloadedBytes);

                    long fileSize = fileInfo.getSize();
                    int chunkSize = 256 * 1024; // 256 KB

                    for (long offset = downloadedBytes; offset < fileSize; offset += chunkSize) {
                        int length = (int) Math.min(chunkSize, fileSize - offset);

                        out.writeUTF("GET " + fileInfo.getHash() + " " + offset + " " + length);
                        out.flush();

                        String response = in.readUTF();
                        if (!response.startsWith("DATA")) {
                            throw new IOException("Unexpected response: " + response);
                        }

                        int dataLength = Integer.parseInt(response.split(" ")[1]);
                        byte[] buffer = new byte[dataLength];
                        in.readFully(buffer);

                        raf.write(buffer);
                        downloadedBytes = offset + dataLength;
                        progress = (int) (downloadedBytes * 100 / fileSize);

                        if (onDownloadUpdate != null) {
                            onDownloadUpdate.accept(this);
                        }
                    }
                }
            } finally {
                socket.close();
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