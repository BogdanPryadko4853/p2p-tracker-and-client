package com.bogdan.tracker.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "peer",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ip", "port"}),
        indexes = @Index(name = "idx_last_seen", columnList = "last_seen"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "files")
public class Peer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private int port;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;

    @ManyToMany
    @JoinTable(
            name = "peer_files",
            joinColumns = @JoinColumn(name = "peer_id"),
            inverseJoinColumns = @JoinColumn(name = "file_hash")
    )
    private List<FileInfo> files = new ArrayList<>();
}