package com.cedu.service;

import com.cedu.dto.transaction.FilterTransactionDto;
import com.cedu.dto.transaction.RequestTransactionDto;
import com.cedu.dto.transaction.ResponseTransactionDto;
import com.cedu.dto.transaction.UpdateTransactionDto;
import com.cedu.entity.Tag;
import com.cedu.mapper.TransactionMapper;
import com.cedu.repository.MoneySourceRepository;
import com.cedu.repository.TagRepository;
import com.cedu.repository.TransactionRepository;
import com.cedu.specification.TransactionSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MoneySourceRepository moneySourceRepository;
    private final TagRepository tagRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            MoneySourceRepository moneySourceRepository,
            TagRepository tagRepository,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.moneySourceRepository = moneySourceRepository;
        this.tagRepository = tagRepository;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Создание новой транзакции
     */
    @Transactional
    public ResponseTransactionDto create(RequestTransactionDto request) {

        var source = moneySourceRepository.findById(request.getMoneySourceId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Money source not found: " + request.getMoneySourceId()));

        var existingTags = new HashSet<Tag>();
        var tagsId = request.getTagsIds();
        if (tagsId != null && !tagsId.isEmpty()) {
            var existing = tagRepository.findAllById(tagsId);
            if (!existing.isEmpty()) {
                existingTags.addAll(existing);
            }
        }

        var transaction = transactionMapper.toEntity(request);
        transaction.setSource(source);
        transaction.setTags(existingTags);

        var saved = transactionRepository.save(transaction);
        return transactionMapper.toDto(saved);
    }

    /**
     *Обновление существующей транзакции
     */
    @Transactional
    public ResponseTransactionDto update(UUID id, UpdateTransactionDto update) {

        var existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Transaction not found: " + id));

        transactionMapper.updateEntity(update, existing);

        if (update.getSourceId() != null) {
            var source = moneySourceRepository.findById(update.getSourceId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Money source not found: " + update.getSourceId()));
            existing.setSource(source);
        }

        if (update.getTagsIds() != null) {
            existing.getTags().clear();
            if (!update.getTagsIds().isEmpty()) {
                var tags = tagRepository.findAllById(update.getTagsIds());
                existing.getTags().addAll(tags);
            }
        }

        var saved = transactionRepository.save(existing);
        return transactionMapper.toDto(saved);
    }

    /**
     * Удаление по id
     */
    @Transactional
    public void delete(UUID id) {
        if (!transactionRepository.existsById(id)) {
            throw new NoSuchElementException("Transaction not found: " + id);
        }
        transactionRepository.deleteById(id);
    }

    /**
     * Получение тегов по фильтру
     */
    @Transactional(readOnly = true)
    public List<ResponseTransactionDto> findAll(FilterTransactionDto filter) {
        return transactionRepository.findAll(TransactionSpecification.withFilters(filter))
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }
}
