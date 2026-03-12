CREATE TABLE IF NOT EXISTS peer (
                                    id UUID PRIMARY KEY,
                                    ip VARCHAR(255) NOT NULL,
                                    port INTEGER NOT NULL,
                                    last_seen TIMESTAMP NOT NULL,
                                    CONSTRAINT unique_ip_port UNIQUE (ip, port)
);

CREATE INDEX idx_last_seen ON peer(last_seen);