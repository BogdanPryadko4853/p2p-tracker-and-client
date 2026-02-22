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

import java.util.UUID;

@Entity
@Table(name = "file_info")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, name = "hash")
    private String hash;

    @Column(nullable = false, name = "size")
    private long size;


/* TODO
    @ManyToMany
    private List<Peer> peers;
*/
}
