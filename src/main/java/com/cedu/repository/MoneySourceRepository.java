package com.cedu.repository;

import com.cedu.entity.MoneySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface MoneySourceRepository extends JpaRepository<MoneySource, UUID>,
        JpaSpecificationExecutor<MoneySource> {}
