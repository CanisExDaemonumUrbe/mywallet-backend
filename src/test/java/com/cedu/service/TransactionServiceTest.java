package com.cedu.service;

import com.cedu.dto.transaction.*;
import com.cedu.entity.MoneySource;
import com.cedu.entity.Tag;
import com.cedu.entity.Transaction;
import com.cedu.mapper.TransactionMapper;
import com.cedu.repository.MoneySourceRepository;
import com.cedu.repository.TagRepository;
import com.cedu.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private MoneySourceRepository moneySourceRepository;
    @Mock private TagRepository tagRepository;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks TransactionService transactionService;

    private UUID userId;
    private UUID sourceId;
    private UUID txId;
    private UUID tag1Id;
    private UUID tag2Id;

    private RequestTransactionDto requestDto;
    private UpdateTransactionDto updateDto;
    private Transaction entity;
    private ResponseTransactionDto responseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sourceId = UUID.randomUUID();
        txId = UUID.randomUUID();
        tag1Id = UUID.randomUUID();
        tag2Id = UUID.randomUUID();

        requestDto = RequestTransactionDto.builder()
                .userId(userId)
                .date(Instant.parse("2024-01-02T10:15:30Z"))
                .amount(new BigDecimal("123.45"))
                .type("expense")
                .moneySourceId(sourceId)
                .description("Lunch")
                .tagsIds(Set.of(tag1Id, tag2Id))
                .build();

        updateDto = UpdateTransactionDto.builder()
                .date(Instant.parse("2024-01-03T12:00:00Z"))
                .amount(new BigDecimal("200.00"))
                .type("income")
                .sourceId(sourceId)
                .description("Salary")
                .tagsIds(Set.of(tag1Id))
                .build();

        entity = new Transaction();
        entity.setId(txId);
        entity.setUserId(userId);
        entity.setDate(requestDto.getDate());
        entity.setAmount(requestDto.getAmount());
        entity.setType(requestDto.getType());
        entity.setDescription(requestDto.getDescription());
        entity.setTags(new LinkedHashSet<>());

        responseDto = ResponseTransactionDto.builder()
                .id(txId)
                .userId(userId)
                .date(entity.getDate())
                .amount(entity.getAmount())
                .type(entity.getType())
                .description(entity.getDescription())
                .tags(Collections.emptySet())
                .build();

    }

    @Test
    void create_ok_setsSourceAndExistingTags_andReturnsDto() {
        var source = new MoneySource();
        source.setId(sourceId);

        var tag1 = new Tag(); tag1.setId(tag1Id);
        var tag2 = new Tag(); tag2.setId(tag2Id);

        when(moneySourceRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(tagRepository.findAllById(Set.of(tag1Id, tag2Id))).thenReturn(List.of(tag1, tag2));

        when(transactionMapper.toEntity(requestDto)).thenReturn(entity);

        when(transactionRepository.save(entity)).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(txId);
            return t;
        });

        when(transactionMapper.toDto(entity)).thenReturn(responseDto);

        var result = transactionService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getSource()).isNotNull();
        assertThat(entity.getTags()).hasSize(2);

        verify(moneySourceRepository).findById(sourceId);
        verify(tagRepository).findAllById(Set.of(tag1Id, tag2Id));
        verify(transactionRepository).save(entity);
        verify(transactionMapper).toEntity(requestDto);
        verify(transactionMapper).toDto(entity);
    }

    @Test
    void create_whenSourceMissing_throws() {
        when(moneySourceRepository.findById(sourceId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.create(requestDto));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void update_ok_updatesFields_source_and_tags() {
        var existing = new Transaction();
        existing.setId(txId);
        existing.setUserId(userId);
        existing.setDate(Instant.parse("2024-01-01T00:00:00Z"));
        existing.setAmount(new BigDecimal("10.00"));
        existing.setType("expense");
        existing.setTags(new LinkedHashSet<>());

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            UpdateTransactionDto dto = inv.getArgument(0);
            Transaction t = inv.getArgument(1);
            if (dto.getDate() != null) t.setDate(dto.getDate());
            if (dto.getAmount() != null) t.setAmount(dto.getAmount());
            if (dto.getType() != null) t.setType(dto.getType());
            if (dto.getDescription() != null) t.setDescription(dto.getDescription());
            return null;
        }).when(transactionMapper).updateEntity(updateDto, existing);

        var source = new MoneySource(); source.setId(sourceId);
        when(moneySourceRepository.findById(sourceId)).thenReturn(Optional.of(source));

        var tag1 = new Tag(); tag1.setId(tag1Id);
        when(tagRepository.findAllById(Set.of(tag1Id))).thenReturn(List.of(tag1));

        when(transactionRepository.save(existing)).thenReturn(existing);

        var updatedDto = ResponseTransactionDto.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .date(updateDto.getDate())
                .amount(updateDto.getAmount())
                .type(updateDto.getType())
                .description(updateDto.getDescription())
                .tags(Collections.emptySet())
                .build();

        when(transactionMapper.toDto(existing)).thenReturn(updatedDto);

        var result = transactionService.update(txId, updateDto);

        assertThat(result).isEqualTo(updatedDto);
        assertThat(existing.getSource()).isNotNull();
        assertThat(existing.getTags()).hasSize(1);

        verify(transactionRepository).findById(txId);
        verify(transactionMapper).updateEntity(updateDto, existing);
        verify(moneySourceRepository).findById(sourceId);
        verify(tagRepository).findAllById(Set.of(tag1Id));
        verify(transactionRepository).save(existing);
        verify(transactionMapper).toDto(existing);
    }

    @Test
    void update_whenTxNotFound_throws() {
        when(transactionRepository.findById(txId)).thenReturn(Optional.empty());
        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.update(txId, updateDto));
    }

    @Test
    void update_whenSourceIdProvidedButMissing_throws() {
        var existing = new Transaction();
        existing.setId(txId);
        existing.setTags(new LinkedHashSet<>());

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));
        when(moneySourceRepository.findById(updateDto.getSourceId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> transactionService.update(txId, updateDto));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void delete_ok() {
        when(transactionRepository.existsById(txId)).thenReturn(true);
        transactionService.delete(txId);
        verify(transactionRepository).deleteById(txId);
    }

    @Test
    void delete_whenMissing_throws() {
        when(transactionRepository.existsById(txId)).thenReturn(false);
        assertThrows(NoSuchElementException.class,
                () -> transactionService.delete(txId));
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void findAll_withFilter_mapsToDto_paged() {
        var pageable = PageRequest.of(0, 20);

        var tx = new Transaction();
        tx.setId(txId);

        // Репозиторий: Specification + Pageable -> Page<Transaction>
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(tx), pageable, 1));

        // Маппинг в DTO
        when(transactionMapper.toDto(tx)).thenReturn(responseDto);

        var filter = FilterTransactionDto.builder()
                .userId(userId)
                .type("expense")
                .build();

        // Сервис: принимает Pageable и возвращает Page<ResponseTransactionDto>
        Page<ResponseTransactionDto> result = transactionService.findAllWithFilter(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(txId);

        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
        verify(transactionMapper).toDto(tx);
    }

}
