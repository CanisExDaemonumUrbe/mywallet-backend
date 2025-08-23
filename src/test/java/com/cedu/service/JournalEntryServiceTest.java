package com.cedu.service;

import com.cedu.dto.journal_entry.FilterJournalEntryDto;
import com.cedu.dto.journal_entry.RequestJournalEntryDto;
import com.cedu.dto.journal_entry.ResponseJournalEntryDto;
import com.cedu.dto.journal_entry.UpdateJournalEntryDto;
import com.cedu.entity.JournalEntry;
import com.cedu.enums.JournalEntryStatus;
import com.cedu.exception.InvalidUserException;
import com.cedu.exception.NotFoundException;
import com.cedu.mapper.JournalEntryMapper;
import com.cedu.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private JournalEntryMapper journalEntryMapper;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private UUID id;
    private UUID userId;
    private UUID reversalOfId;

    private RequestJournalEntryDto requestDto;
    private JournalEntry entity;
    private ResponseJournalEntryDto responseDto;

    private final OffsetDateTime T0 = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        userId = UUID.randomUUID();
        reversalOfId = UUID.randomUUID();

        // request
        requestDto = RequestJournalEntryDto.builder()
                .userId(userId)
                .occurredAt(T0)
                .bookedAt(T0.plusHours(1))
                .description("Test entry")
                .reversalOfId(null) // по умолчанию без реверсала
                .build();

        // entity
        entity = new JournalEntry();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setOccurredAt(T0);
        entity.setBookedAt(T0.plusHours(1));
        entity.setDescription("Test entry");

        // response
        responseDto = ResponseJournalEntryDto.builder()
                .userId(userId)
                .occurredAt(T0)
                .bookedAt(T0.plusHours(1))
                .description("Test entry")
                .build();
    }

    @Test
    void create_withoutReversal_ok() {
        when(journalEntryMapper.toEntity(requestDto)).thenReturn(entity);
        when(journalEntryRepository.save(entity)).thenReturn(entity);
        when(journalEntryMapper.toDto(entity)).thenReturn(responseDto);

        var result = journalEntryService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getReversalOf()).isNull();
        assertThat(entity.getReversalOfId()).isNull();

        verify(journalEntryMapper).toEntity(requestDto);
        verify(journalEntryRepository).save(entity);
        verify(journalEntryMapper).toDto(entity);
        verify(journalEntryRepository, never()).findById(any());
    }

    @Test
    void create_withReversal_ok() {
        // запрос с реверсалом
        var req = RequestJournalEntryDto.builder()
                .userId(userId)
                .occurredAt(T0)
                .bookedAt(T0.plusHours(1))
                .description("With reversal")
                .reversalOfId(reversalOfId)
                .build();

        var reversal = new JournalEntry();
        reversal.setId(reversalOfId);
        reversal.setUserId(userId); // тот же userId — валидно

        when(journalEntryRepository.findById(reversalOfId)).thenReturn(Optional.of(reversal));
        when(journalEntryMapper.toEntity(req)).thenReturn(entity);
        when(journalEntryRepository.save(entity)).thenReturn(entity);
        when(journalEntryMapper.toDto(entity)).thenReturn(responseDto);

        var result = journalEntryService.create(req);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getReversalOf()).isEqualTo(reversal);
        assertThat(entity.getReversalOfId()).isEqualTo(reversalOfId);

        verify(journalEntryRepository).findById(reversalOfId);
        verify(journalEntryMapper).toEntity(req);
        verify(journalEntryRepository).save(entity);
        verify(journalEntryMapper).toDto(entity);
    }

    @Test
    void create_whenReversalNotFound_throws() {
        var req = RequestJournalEntryDto.builder()
                .userId(userId)
                .occurredAt(T0)
                .reversalOfId(reversalOfId)
                .build();

        when(journalEntryRepository.findById(reversalOfId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> journalEntryService.create(req));

        verify(journalEntryRepository).findById(reversalOfId);
        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    void create_whenUserMismatch_throws() {
        var req = RequestJournalEntryDto.builder()
                .userId(userId)                // userId в запросе
                .occurredAt(T0)
                .reversalOfId(reversalOfId)
                .build();

        var reversal = new JournalEntry();
        reversal.setId(reversalOfId);
        reversal.setUserId(UUID.randomUUID()); // другой пользователь

        when(journalEntryRepository.findById(reversalOfId)).thenReturn(Optional.of(reversal));

        assertThrows(InvalidUserException.class, () -> journalEntryService.create(req));

        verify(journalEntryRepository).findById(reversalOfId);
        verify(journalEntryRepository, never()).save(any());
        verify(journalEntryMapper, never()).toEntity(any());
    }

    @Test
    void update_ok_mapsAndSave() {
        var id = UUID.randomUUID();

        // существующая сущность в БД
        var existing = new JournalEntry();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setOccurredAt(T0);
        existing.setBookedAt(T0.plusHours(1));
        existing.setDescription("Old desc");
        existing.setStatus(JournalEntryStatus.POSTED);

        when(journalEntryRepository.findById(id)).thenReturn(Optional.of(existing));

        // входящее обновление
        var update = UpdateJournalEntryDto.builder()
                .status(JournalEntryStatus.VOID)
                .build();

        // эмулируем работу маппера: он обновляет поля existing на основе update
        doAnswer(inv -> {
            UpdateJournalEntryDto dto = inv.getArgument(0);
            JournalEntry target = inv.getArgument(1);
            if (dto.getStatus() != null) target.setStatus(dto.getStatus());
            return null;
        }).when(journalEntryMapper).updateEntity(eq(update), eq(existing));

        when(journalEntryRepository.save(existing)).thenReturn(existing);

        var updatedDto = ResponseJournalEntryDto.builder()
                .userId(existing.getUserId())
                .occurredAt(existing.getOccurredAt())
                .bookedAt(T0.plusHours(2))
                .description("New desc")
                .status(JournalEntryStatus.VOID)
                .build();

        when(journalEntryMapper.toDto(existing)).thenReturn(updatedDto);

        var result = journalEntryService.update(id, update);

        // проверяем, что всё смэпилось и сохранилось
        assertThat(result).isEqualTo(updatedDto);
        assertThat(existing.getStatus()).isEqualTo(JournalEntryStatus.VOID);

        verify(journalEntryRepository).findById(id);
        verify(journalEntryMapper).updateEntity(update, existing);
        verify(journalEntryRepository).save(existing);
        verify(journalEntryMapper).toDto(existing);
    }


    @Test
    void update_whenNotFound_throws() {
        var id = UUID.randomUUID();
        var update = UpdateJournalEntryDto.builder()
                .status(JournalEntryStatus.POSTED)
                .build();

        when(journalEntryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> journalEntryService.update(id, update));

        verify(journalEntryRepository).findById(id);
        verify(journalEntryMapper, never()).updateEntity(any(), any());
        verify(journalEntryRepository, never()).save(any());
        verify(journalEntryMapper, never()).toDto(any());
    }

    @Test
    void find_withFilters_returnsMappedPage() {
        var f = FilterJournalEntryDto.builder()
                .userId(userId)
                .build();

        var pageable = PageRequest.of(0, 10, Sort.by("occurredAt").descending());

        var e1 = new JournalEntry(); e1.setId(UUID.randomUUID()); e1.setUserId(userId); e1.setOccurredAt(T0.plusDays(1));
        var e2 = new JournalEntry(); e2.setId(UUID.randomUUID()); e2.setUserId(userId); e2.setOccurredAt(T0.plusDays(2));

        var page = new PageImpl<>(List.of(e1, e2), pageable, 2);

        when(journalEntryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        var d1 = ResponseJournalEntryDto.builder().userId(userId).occurredAt(e1.getOccurredAt()).build();
        var d2 = ResponseJournalEntryDto.builder().userId(userId).occurredAt(e2.getOccurredAt()).build();

        when(journalEntryMapper.toDto(e1)).thenReturn(d1);
        when(journalEntryMapper.toDto(e2)).thenReturn(d2);

        Page<ResponseJournalEntryDto> result = journalEntryService.find(f, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(d1, d2);

        // проверяем, что в репозиторий ушёл именно переданный pageable
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(journalEntryRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);

        verify(journalEntryMapper).toDto(e1);
        verify(journalEntryMapper).toDto(e2);
    }


}
