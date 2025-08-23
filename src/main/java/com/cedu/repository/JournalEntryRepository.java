package com.cedu.repository;

import com.cedu.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID>,
        JpaSpecificationExecutor<JournalEntry> {}
