package com.bogdan.tracker.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "peer")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Peer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false, name = "ip_addres")
    private String ip;

    @Column(unique = true, nullable = false, name = "port")
    private int port;

    @Column(nullable = false, name = "last_seen")
    private LocalDateTime lastSeen;

/* TODO
    @ManyToMany
    private List<FileInfo> files;
 */

}
