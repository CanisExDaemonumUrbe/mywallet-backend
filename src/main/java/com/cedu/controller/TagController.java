package com.cedu.controller;

import com.cedu.dto.tag.FilterTagDto;
import com.cedu.dto.tag.RequestTagDto;
import com.cedu.dto.tag.ResponseTagDto;
import com.cedu.dto.tag.UpdateTagDto;
import com.cedu.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<ResponseTagDto> create(@RequestBody RequestTagDto requestTagDto) {
        var createdTag = tagService.create(requestTagDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseTagDto> update(
            @PathVariable UUID id,
            @RequestBody UpdateTagDto updateTagDto
    ) {
        var updated = tagService.update(id, updateTagDto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseTagDto> delete(@PathVariable UUID id) {
        tagService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<Page<ResponseTagDto>> getAll(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var filterDto = FilterTagDto.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .build();

        var page = tagService.findAllWithFilter(filterDto, pageable);
        return ResponseEntity.ok(page);
    }
}
