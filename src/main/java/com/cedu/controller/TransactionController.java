package com.cedu.controller;

import com.cedu.dto.transaction.FilterTransactionDto;
import com.cedu.dto.transaction.RequestTransactionDto;
import com.cedu.dto.transaction.ResponseTransactionDto;
import com.cedu.dto.transaction.UpdateTransactionDto;
import com.cedu.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ResponseTransactionDto> create(
            @RequestBody RequestTransactionDto request
    ) {
        var created = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseTransactionDto> update(
            @PathVariable UUID id,
            @RequestBody UpdateTransactionDto update
    ) {
        var updated = transactionService.update(id, update);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<ResponseTransactionDto>> getAll(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID sourceId,
            @RequestParam(required = false) Set<UUID> tagsId
    ) {
        var filter = FilterTransactionDto.builder()
                .id(id)
                .userId(userId)
                .from(from)
                .to(to)
                .type(type)
                .sourceId(sourceId)
                .tagsId(tagsId)
                .build();

        var transactions = transactionService.findAll(filter);
        return ResponseEntity.status(HttpStatus.OK).body(transactions);
    }
}
