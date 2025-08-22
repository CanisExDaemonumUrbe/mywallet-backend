package com.cedu.service;

import com.cedu.dto.account.FilterAccountDto;
import com.cedu.dto.account.RequestAccountDto;
import com.cedu.dto.account.ResponseAccountDto;
import com.cedu.dto.account.UpdateAccountDto;
import com.cedu.entity.Account;
import com.cedu.enums.AccountKind;
import com.cedu.exception.InvalidAccountKindException;
import com.cedu.exception.InvalidUserException;
import com.cedu.exception.NotFoundException;
import com.cedu.mapper.AccountMapper;
import com.cedu.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private UUID id;
    private UUID userId;
    private UUID parentId;

    private RequestAccountDto requestDto;
    private UpdateAccountDto updateDto;

    private Account entity;
    private ResponseAccountDto responseDto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        userId = UUID.randomUUID();
        parentId = UUID.randomUUID();

        requestDto = RequestAccountDto.builder()
                .userId(userId)
                .parentId(null)// по умолчанию без родителя
                .name("Cash")
                .kind(AccountKind.ASSET)
                .isActive(true)
                .build();

        updateDto = UpdateAccountDto.builder()
                .name("Cash (updated)")
                .isActive(false)
                .build();

        entity = new Account();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setName("Cash");
        entity.setKind(AccountKind.ASSET);
        entity.setIsActive(true);

        responseDto = ResponseAccountDto.builder()
                .id(id)
                .userId(userId)
                .name(entity.getName())
                .kind(entity.getKind())
                .active(entity.getIsActive())
                .parentId(null)
                .build();
    }

    @Test
    void create_withoutParent_ok() {
        when(accountMapper.toEntity(requestDto)).thenReturn(entity);
        when(accountRepository.save(entity)).thenReturn(entity);
        when(accountMapper.toDto(entity)).thenReturn(responseDto);

        var result = accountService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getParent()).isNull();

        verify(accountMapper).toEntity(requestDto);
        verify(accountRepository).save(entity);
        verify(accountMapper).toDto(entity);
        verify(accountRepository, never()).findById(any());
    }

    @Test
    void create_withParent_ok() {
        // request с родителем
        requestDto = RequestAccountDto.builder()
                .parentId(parentId)
                .kind(AccountKind.ASSET)
                .userId(userId)
                .build();

        var parent = new Account();
        parent.setId(parentId);
        parent.setUserId(userId);
        parent.setKind(AccountKind.ASSET);

        when(accountRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(accountMapper.toEntity(requestDto)).thenReturn(entity);
        when(accountRepository.save(entity)).thenReturn(entity);
        when(accountMapper.toDto(entity)).thenReturn(responseDto);

        var result = accountService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getParent()).isNotNull();
        assertThat(entity.getParent().getId()).isEqualTo(parentId);

        verify(accountRepository).findById(parentId);
        verify(accountMapper).toEntity(requestDto);
        verify(accountRepository).save(entity);
        verify(accountMapper).toDto(entity);
    }

    @Test
    void create_whenParentNotFound_throws() {
        requestDto = RequestAccountDto.builder()
                .parentId(parentId)
                .kind(AccountKind.ASSET)
                .userId(UUID.randomUUID())
                .build();

        when(accountRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.create(requestDto));
        verify(accountRepository).findById(parentId);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void create_whenKindMismatch_throws() {
        requestDto = RequestAccountDto.builder()
                .parentId(parentId)
                .kind(AccountKind.EQUITY)
                .userId(userId)
                .build();

        var parent = new Account();
        parent.setId(parentId);
        parent.setUserId(userId);
        parent.setKind(AccountKind.ASSET);

        when(accountRepository.findById(parentId)).thenReturn(Optional.of(parent));

        assertThrows(InvalidAccountKindException.class, () -> accountService.create(requestDto));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void create_whenUserMismatch_throws() {
        requestDto = RequestAccountDto.builder()
                .parentId(parentId)
                .kind(AccountKind.ASSET)
                .userId(UUID.randomUUID())
                .build();

        var parent = new Account();
        parent.setId(parentId);
        parent.setUserId(UUID.randomUUID()); // другой пользователь
        parent.setKind(AccountKind.ASSET);

        when(accountRepository.findById(parentId)).thenReturn(Optional.of(parent));

        assertThrows(InvalidUserException.class, () -> accountService.create(requestDto));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void update_ok() {
        var existing = new Account();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setName("Cash");
        existing.setKind(AccountKind.ASSET);
        existing.setIsActive(true);

        when(accountRepository.findById(id)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            UpdateAccountDto dto = inv.getArgument(0);
            Account target = inv.getArgument(1);
            if (dto.getName() != null) target.setName(dto.getName());
            if (dto.getIsActive() != null) target.setIsActive(dto.getIsActive());
            return null;
        }).when(accountMapper).updateEntity(updateDto, existing);

        when(accountRepository.save(existing)).thenReturn(existing);

        var updatedDto = ResponseAccountDto.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .name("Cash (updated)")
                .kind(existing.getKind())
                .active(false)
                .parentId(existing.getParentId())
                .build();

        when(accountMapper.toDto(existing)).thenReturn(updatedDto);

        var result = accountService.update(id, updateDto);

        assertThat(result).isEqualTo(updatedDto);
        assertThat(existing.getName()).isEqualTo("Cash (updated)");
        assertThat(existing.getIsActive()).isFalse();

        verify(accountRepository).findById(id);
        verify(accountMapper).updateEntity(updateDto, existing);
        verify(accountRepository).save(existing);
        verify(accountMapper).toDto(existing);
    }

    @Test
    void update_whenNotFound_throws() {
        when(accountRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> accountService.update(id, updateDto));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void find_withFilters_returnsMappedPage() {
        var filter = FilterAccountDto.builder()
                .userId(userId)
                .kind(AccountKind.ASSET)
                .isActive(true)
                .build();

        var pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        var a1 = new Account(); a1.setId(UUID.randomUUID()); a1.setUserId(userId); a1.setName("A"); a1.setKind(AccountKind.ASSET); a1.setIsActive(true);
        var a2 = new Account(); a2.setId(UUID.randomUUID()); a2.setUserId(userId); a2.setName("B"); a2.setKind(AccountKind.ASSET); a2.setIsActive(true);

        var page = new PageImpl<>(List.of(a1, a2), pageable, 2);

        when(accountRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        var dto1 = ResponseAccountDto.builder().id(a1.getId()).userId(userId).name("A").kind(AccountKind.ASSET).active(true).build();
        var dto2 = ResponseAccountDto.builder().id(a2.getId()).userId(userId).name("B").kind(AccountKind.ASSET).active(true).build();

        when(accountMapper.toDto(a1)).thenReturn(dto1);
        when(accountMapper.toDto(a2)).thenReturn(dto2);

        Page<ResponseAccountDto> result = accountService.find(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(dto1, dto2);

        verify(accountRepository).findAll(any(Specification.class), eq(pageable));
        verify(accountMapper).toDto(a1);
        verify(accountMapper).toDto(a2);
    }
}
