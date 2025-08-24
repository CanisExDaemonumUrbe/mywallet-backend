package com.cedu.repository;

import com.cedu.entity.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PostingRepository extends JpaRepository<Posting, UUID>,
        JpaSpecificationExecutor<Posting> {}
