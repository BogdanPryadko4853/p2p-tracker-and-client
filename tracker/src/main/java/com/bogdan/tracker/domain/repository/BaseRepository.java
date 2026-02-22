package com.bogdan.tracker.domain.repository;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T,ID>{
    List<T> findAll();
    Optional<T> findById(ID id);
    void save(T t);
    void deleteById(T t);
    boolean existsById(ID id);
    long count();
}
