package com.bogdan.tracker.infrastructure.repository.jpa;

import com.bogdan.tracker.domain.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileInfoJpaRepository extends JpaRepository<FileInfo, String> {
}
