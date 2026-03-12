
CREATE TABLE IF NOT EXISTS file_info (
                                         hash VARCHAR(255) PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL,
                                         size BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS peer_files (
                                          peer_id UUID NOT NULL,
                                          file_hash VARCHAR(255) NOT NULL,
                                          PRIMARY KEY (peer_id, file_hash),
                                          FOREIGN KEY (peer_id) REFERENCES peer(id) ON DELETE CASCADE,
                                          FOREIGN KEY (file_hash) REFERENCES file_info(hash) ON DELETE CASCADE
);

CREATE INDEX idx_peer_files_peer_id ON peer_files(peer_id);
CREATE INDEX idx_peer_files_file_hash ON peer_files(file_hash);