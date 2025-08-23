package com.cedu.controller;

import com.cedu.dto.account.FilterAccountDto;
import com.cedu.dto.account.RequestAccountDto;
import com.cedu.dto.account.ResponseAccountDto;
import com.cedu.dto.account.UpdateAccountDto;
import com.cedu.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseAccountDto> create(
            @RequestBody RequestAccountDto request
    ) {
        var created = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseAccountDto> update(
            @PathVariable UUID id,
            @RequestBody UpdateAccountDto update
    ) {
        var updated = accountService.update(id, update);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseAccountDto> delete(
            @PathVariable UUID id
    ) {
        accountService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ResponseAccountDto>> search(
            @RequestBody FilterAccountDto filter,
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        var page = accountService.find(filter, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }
}
