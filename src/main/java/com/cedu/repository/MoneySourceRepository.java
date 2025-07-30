package com.cedu.repository;

import com.cedu.entity.MoneySource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MoneySourceRepository extends JpaRepository<MoneySource, UUID> {
    List<MoneySource> findAllByUserId(UUID userId);
}
