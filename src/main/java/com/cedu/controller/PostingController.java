package com.cedu.controller;

import com.cedu.dto.posting.FilterPostingDto;
import com.cedu.dto.posting.RequestPostingDto;
import com.cedu.dto.posting.ResponsePostingDto;
import com.cedu.service.PostingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/postings")
public class PostingController {
    private final PostingService postingService;

    public PostingController(PostingService postingService) {
        this.postingService = postingService;
    }

    @PostMapping("")
    public ResponseEntity<ResponsePostingDto> create (
            @RequestBody RequestPostingDto request
    ) {
        var created = postingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponsePostingDto> delete (
            @PathVariable UUID id
    ) {
        postingService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ResponsePostingDto>> search (
            @RequestBody FilterPostingDto filter,
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        var page = postingService.find(filter, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }
}
