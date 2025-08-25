package com.cedu.controller;

import com.cedu.dto.journal_entry.FilterJournalEntryDto;
import com.cedu.dto.journal_entry.RequestJournalEntryDto;
import com.cedu.dto.journal_entry.ResponseJournalEntryDto;
import com.cedu.dto.journal_entry.UpdateJournalEntryDto;
import com.cedu.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/journal")
public class JournalEntryController {

    private JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseJournalEntryDto> create (
            @RequestBody RequestJournalEntryDto request
    ) {
        var created = journalEntryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseJournalEntryDto> update (
            @PathVariable UUID id,
            @RequestBody UpdateJournalEntryDto update
    ) {
        var updated = journalEntryService.update(id, update);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseJournalEntryDto> delete (
            @PathVariable UUID id
    ) {
        journalEntryService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ResponseJournalEntryDto>> search (
            @RequestBody FilterJournalEntryDto filter,
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        var page = journalEntryService.find(filter, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }
}
