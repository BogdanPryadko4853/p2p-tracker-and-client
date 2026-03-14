package com.bogdan.client.p2p;

import com.bogdan.client.common.ClientConfigConstant;
import com.bogdan.client.dto.FileInfoDto;
import com.bogdan.client.dto.PeerResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class P2PService {
    @Setter
    private Consumer<DownloadTask> onDownloadUpdate;
    private final ClientConfigConstant clientConfigConstant;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Map<String, DownloadTask> downloads = new ConcurrentHashMap<>();
    private final Map<String, FileInfoDto> sharedFiles = new ConcurrentHashMap<>();

    public void startServer() {
        executorService = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(clientConfigConstant.PORT);
            log.info("P2P server started on port {}", clientConfigConstant.PORT);

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
            DataInputStream in = null;
            DataOutputStream out = null;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    String command;
                    try {
                        command = in.readUTF();
                    } catch (EOFException e) {
                        log.debug("Client closed connection");
                        break;
                    }

                    if (command == null) break;

                    if (command.startsWith("GET")) {
                        String[] parts = command.split(" ");
                        if (parts.length < 4) {
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
                    log.error("Error handling client: {}", e.getMessage());
                }
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Error closing socket: {}", e.getMessage());
                }
            }
        }

        private void handleFileRequest(String fileHash, long offset, int length, DataOutputStream out) throws IOException {
            FileInfoDto fileInfo = sharedFiles.get(fileHash);
            if (fileInfo == null) {
                out.writeUTF("ERROR File not found");
                return;
            }

            File file = new File(clientConfigConstant.SHARED_DIR + "/" + fileInfo.getName());
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

    public void downloadFile(FileInfoDto fileInfo, PeerResponse peer, String fileName, String downloadId) {
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
            downloadPath = clientConfigConstant.DOWNLOAD_DIR+ "/" + fileName;
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
                Path fullPath = Paths.get(downloadPath).toAbsolutePath().normalize();
                log.info("Download completed: {} saved to {}", fileName, fullPath);

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