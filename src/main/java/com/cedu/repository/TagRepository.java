package com.cedu.repository;

import com.cedu.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID>,
        JpaSpecificationExecutor<Tag> {}