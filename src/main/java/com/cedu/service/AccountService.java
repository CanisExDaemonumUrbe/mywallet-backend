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
import com.cedu.specification.AccountSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public ResponseAccountDto create(RequestAccountDto request) {
        UUID parentId = request.getParentId();

        Account parent = null;
        if (parentId != null) {
            parent = accountRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent not found: " + parentId));
        }

        if (parent != null) {
            AccountKind requestKind = request.getKind();
            AccountKind parentKind = parent.getKind();
            if (!requestKind.equals(parentKind)) {
                throw new InvalidAccountKindException(
                        "Invalid kind: " + requestKind + ", expected: " + parentKind
                );
            }

            UUID requestUserId = request.getUserId();
            UUID parentUserId = parent.getUserId();
            if (!requestUserId.equals(parentUserId)) {
                throw new InvalidUserException(
                        "user_id mismatch: request=" + requestUserId + ", expected=" + parentUserId
                );
            }
        }

        var account = accountMapper.toEntity(request);
        account.setParent(parent);
        var saved = accountRepository.save(account);
        return accountMapper.toDto(saved);
    }


    @Transactional
    public ResponseAccountDto update(UUID id, UpdateAccountDto update) {
        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found: " + id));

        accountMapper.updateEntity(update, existing);
        Account updated = accountRepository.save(existing);
        ResponseAccountDto response = accountMapper.toDto(updated);
        return response;
    }

    //НЕ ТЕСТИРОВАТЬ это метод
    @Transactional
    public void delete(UUID id) {
        //Изменить логику умного удаления

        if (!accountRepository.existsById(id)) {
            throw new NotFoundException("Account not found: " + id);
        }
        accountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ResponseAccountDto> find(
            FilterAccountDto filter,
            Pageable pageable
    ) {
        var spec = AccountSpecification.withFilters(filter);
        return accountRepository.findAll(spec, pageable)
                .map(accountMapper::toDto);
    }
}
