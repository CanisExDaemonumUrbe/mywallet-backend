package com.cedu.service;

import com.cedu.dto.posting.FilterPostingDto;
import com.cedu.dto.posting.RequestPostingDto;
import com.cedu.dto.posting.ResponsePostingDto;
import com.cedu.entity.Account;
import com.cedu.entity.JournalEntry;
import com.cedu.entity.Posting;
import com.cedu.enums.PostingSide;
import com.cedu.exception.NotFoundException;
import com.cedu.mapper.PostingMapper;
import com.cedu.repository.AccountRepository;
import com.cedu.repository.JournalEntryRepository;
import com.cedu.repository.PostingRepository;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostingServiceTest {

    private static final BigDecimal AMOUNT_123_45 = new BigDecimal("123.45");
    private static final PostingSide DEBIT = PostingSide.DEBIT;
    private static final PostingSide CREDIT = PostingSide.CREDIT;

    @Mock
    private PostingRepository postingRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private PostingMapper postingMapper;

    @Mock
    private OwnershipValidator ownershipValidator;

    @InjectMocks
    private PostingService postingService;

    private UUID userId;
    private UUID accountId;
    private UUID journalEntryId;

    private Account account;
    private JournalEntry journalEntry;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();

        account = new Account();
        account.setId(accountId);
        account.setUserId(userId);

        journalEntry = new JournalEntry();
        journalEntry.setId(journalEntryId);
        journalEntry.setUserId(userId);
    }

    // -------- create(...) --------

    @Test
    void create_shouldSaveAndReturnDto_whenOwnershipValid() {
        // given
        RequestPostingDto request = RequestPostingDto.builder()
                .userId(userId)
                .accountId(accountId)
                .journalEntryId(journalEntryId)
                .side(DEBIT)
                .amount(AMOUNT_123_45)
                .build();

        Posting mappedEntity = new Posting();
        mappedEntity.setSide(DEBIT);
        mappedEntity.setAmount(AMOUNT_123_45);

        Posting savedEntity = new Posting();
        UUID postingId = UUID.randomUUID();
        savedEntity.setId(postingId);
        savedEntity.setSide(DEBIT);
        savedEntity.setAmount(AMOUNT_123_45);
        savedEntity.setAccount(account);
        savedEntity.setJournalEntry(journalEntry);

        ResponsePostingDto expectedDto = ResponsePostingDto.builder()
                .id(postingId)
                .side(DEBIT)
                .amount(AMOUNT_123_45)
                .accountId(accountId)
                .journalEntryId(journalEntryId)
                .build();

        when(ownershipValidator.findAndValidate(eq(accountId), any(), any(), eq(userId), eq("Account")))
                .thenReturn(account);
        when(ownershipValidator.findAndValidate(eq(journalEntryId), any(), any(), eq(userId), eq("JournalEntry")))
                .thenReturn(journalEntry);
        when(postingMapper.toEntity(request)).thenReturn(mappedEntity);
        when(postingRepository.save(any(Posting.class))).thenReturn(savedEntity);
        when(postingMapper.toDto(savedEntity)).thenReturn(expectedDto);

        // when
        ResponsePostingDto result = postingService.create(request);

        // then
        assertNotNull(result);
        assertEquals(postingId, result.getId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(journalEntryId, result.getJournalEntryId());
        assertEquals(AMOUNT_123_45, result.getAmount());

        ArgumentCaptor<Posting> captor = ArgumentCaptor.forClass(Posting.class);
        verify(postingRepository).save(captor.capture());
        Posting toSave = captor.getValue();
        assertSame(account, toSave.getAccount());
        assertSame(journalEntry, toSave.getJournalEntry());
        assertEquals(DEBIT, toSave.getSide());
        assertEquals(AMOUNT_123_45, toSave.getAmount());

        verify(postingMapper).toEntity(request);
        verify(postingMapper).toDto(savedEntity);
    }

    @Test
    void create_shouldThrow_whenAccountNotFoundOrNotOwned() {
        RequestPostingDto request = new RequestPostingDto();
        request.setUserId(userId);
        request.setAccountId(accountId);
        request.setJournalEntryId(journalEntryId);

        when(ownershipValidator.findAndValidate(eq(accountId), any(), any(), eq(userId), eq("Account")))
                .thenThrow(new NotFoundException("Account not found or not owned"));

        assertThrows(NotFoundException.class, () -> postingService.create(request));

        verify(postingMapper, never()).toEntity(any());
        verify(postingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenJournalEntryNotFoundOrNotOwned() {
        RequestPostingDto request = new RequestPostingDto();
        request.setUserId(userId);
        request.setAccountId(accountId);
        request.setJournalEntryId(journalEntryId);

        when(ownershipValidator.findAndValidate(eq(accountId), any(), any(), eq(userId), eq("Account")))
                .thenReturn(account);

        when(ownershipValidator.findAndValidate(eq(journalEntryId), any(), any(), eq(userId), eq("JournalEntry")))
                .thenThrow(new NotFoundException("JournalEntry not found or not owned"));

        assertThrows(NotFoundException.class, () -> postingService.create(request));

        verify(postingMapper, never()).toEntity(any());
        verify(postingRepository, never()).save(any());
    }

    // -------- find(...) --------

    @Test
    void find_shouldReturnMappedPage() {
        FilterPostingDto filter = FilterPostingDto.builder().build();
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());

        Posting p1 = new Posting();
        p1.setId(UUID.randomUUID());
        Posting p2 = new Posting();
        p2.setId(UUID.randomUUID());

        List<Posting> entities = List.of(p1, p2);
        Page<Posting> page = new PageImpl<>(entities, pageable, 10);

        ResponsePostingDto d1 = ResponsePostingDto.builder()
                .id(p1.getId())
                .build();

        ResponsePostingDto d2 = ResponsePostingDto.builder()
                .id(p2.getId())
                .build();

        when(postingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(postingMapper.toDto(p1)).thenReturn(d1);
        when(postingMapper.toDto(p2)).thenReturn(d2);

        Page<ResponsePostingDto> result = postingService.find(filter, pageable);

        assertNotNull(result);
        assertEquals(2, result.getNumberOfElements());
        assertEquals(10, result.getTotalElements());
        assertEquals(List.of(d1, d2), result.getContent());

        verify(postingRepository).findAll(any(Specification.class), eq(pageable));
        verify(postingMapper).toDto(p1);
        verify(postingMapper).toDto(p2);
    }
}
