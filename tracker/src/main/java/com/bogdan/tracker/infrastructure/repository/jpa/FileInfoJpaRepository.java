package com.bogdan.tracker.infrastructure.repository.jpa;

import com.bogdan.tracker.domain.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileInfoJpaRepository extends JpaRepository<FileInfo, String> {
    List<FileInfo> findByNameContainingIgnoreCase(String name);

    @Query("SELECT f FROM FileInfo f JOIN f.peers p WHERE p.id = :peerId")
    List<FileInfo> findByPeerId(@Param("peerId") String peerId);

    @Query("SELECT f FROM FileInfo f JOIN f.peers p WHERE f.hash = :hash AND p.id = :peerId")
    Optional<FileInfo> findByHashAndPeerId(@Param("hash") String hash, @Param("peerId") String peerId);
}
