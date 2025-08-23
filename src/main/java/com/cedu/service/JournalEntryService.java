package com.cedu.service;

import com.cedu.dto.journal_entry.FilterJournalEntryDto;
import com.cedu.dto.journal_entry.RequestJournalEntryDto;
import com.cedu.dto.journal_entry.ResponseJournalEntryDto;
import com.cedu.dto.journal_entry.UpdateJournalEntryDto;
import com.cedu.entity.JournalEntry;
import com.cedu.exception.InvalidUserException;
import com.cedu.exception.NotFoundException;
import com.cedu.mapper.JournalEntryMapper;
import com.cedu.repository.JournalEntryRepository;
import com.cedu.specification.JournalEntrySpecification;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryMapper journalEntryMapper;

    public JournalEntryService(JournalEntryRepository journalEntryRepository, JournalEntryMapper journalEntryMapper) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryMapper = journalEntryMapper;
    }

    @Transactional
    public ResponseJournalEntryDto create(RequestJournalEntryDto request) {
        UUID reversalOfId = request.getReversalOfId();

        JournalEntry reversalOf = null;
        if (reversalOfId != null) {
            reversalOf = journalEntryRepository.findById(reversalOfId)
                    .orElseThrow(() -> new NotFoundException("Reversal Journal Entry not found: " + reversalOfId));
        }

        if (reversalOf != null) {
            UUID requestUserId = request.getUserId();
            UUID reversalOfUserId = reversalOf.getUserId();
            if (!requestUserId.equals(reversalOfUserId)) {
                throw new InvalidUserException(
                        "user_id mismatch: request=" + requestUserId + ", expected=" + reversalOfUserId
                );
            }
        }

        var journalEntry = journalEntryMapper.toEntity(request);
        journalEntry.setReversalOf(reversalOf);
        var saved = journalEntryRepository.save(journalEntry);
        return journalEntryMapper.toDto(saved);

    }

    @Transactional
    public ResponseJournalEntryDto update(UUID id, UpdateJournalEntryDto update) {
        JournalEntry existing = journalEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Journal Entry not found: " + id));

        journalEntryMapper.updateEntity(update, existing);
        JournalEntry updated = journalEntryRepository.save(existing);
        ResponseJournalEntryDto response = journalEntryMapper.toDto(updated);
        return response;
    }

    //НЕ ТЕСТИРОВАТЬ это метод
    @Transactional
    public void delete(UUID id) {
        //Только внутренний метод, не доступен клиенту

        if (!journalEntryRepository.existsById(id)) {
            throw new NotFoundException("Journal Entry not found: " + id);
        }
        journalEntryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ResponseJournalEntryDto> find(
            FilterJournalEntryDto filter,
            Pageable pageable
    ) {
        var spec = JournalEntrySpecification.withFilters(filter);
        return journalEntryRepository.findAll(spec, pageable)
                .map(journalEntryMapper::toDto);
    }

}
