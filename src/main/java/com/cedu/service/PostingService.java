package com.cedu.service;

import com.cedu.dto.posting.FilterPostingDto;
import com.cedu.dto.posting.RequestPostingDto;
import com.cedu.dto.posting.ResponsePostingDto;
import com.cedu.entity.Account;
import com.cedu.entity.JournalEntry;
import com.cedu.exception.NotFoundException;
import com.cedu.mapper.PostingMapper;
import com.cedu.repository.AccountRepository;
import com.cedu.repository.JournalEntryRepository;
import com.cedu.repository.PostingRepository;
import com.cedu.specification.PostingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostingService {

    private final PostingRepository postingRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final PostingMapper postingMapper;
    private final OwnershipValidator ownershipValidator;

    @Transactional
    public ResponsePostingDto create(RequestPostingDto request) {
        UUID requestUserId = request.getUserId();

        Account account = ownershipValidator.findAndValidate(
          request.getAccountId(),
          accountRepository::findById,
          Account::getUserId,
          requestUserId,
          "Account"
        );

        JournalEntry journalEntry = ownershipValidator.findAndValidate(
                request.getJournalEntryId(),
                journalEntryRepository::findById,
                JournalEntry::getUserId,
                requestUserId,
                "JournalEntry"
        );

        var posting = postingMapper.toEntity(request);
        posting.setAccount(account);
        posting.setJournalEntry(journalEntry);
        var saved = postingRepository.save(posting);
        return postingMapper.toDto(saved);
    }

    //НЕ ТЕСТИРОВАТЬ это метод
    @Transactional
    public void delete(UUID id) {
        //Только внутренний метод, не доступен клиенту

        if (!postingRepository.existsById(id)) {
            throw new NotFoundException("Posting with id " + id + " not found");
        }
        postingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ResponsePostingDto> find(
            FilterPostingDto filter,
            Pageable pageable
    ) {
        var spec = PostingSpecification.withFilters(filter);
        return postingRepository.findAll(spec, pageable)
                .map(postingMapper::toDto);
    }
}
