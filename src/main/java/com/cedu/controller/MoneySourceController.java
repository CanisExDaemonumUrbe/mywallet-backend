package com.cedu.controller;

import com.cedu.dto.money_source.MoneySourceFilterDto;
import com.cedu.dto.money_source.RequestMoneySourceDto;
import com.cedu.dto.money_source.ResponseMoneySourceDto;
import com.cedu.dto.money_source.UpdateMoneySourceDto;
import com.cedu.service.MoneySourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sources")
public class MoneySourceController {

    private final MoneySourceService moneySourceService;

    public MoneySourceController(MoneySourceService moneySourceService) {
        this.moneySourceService = moneySourceService;
    }

    /**
     * Создание нового источника
     */
    @PostMapping("")
    public ResponseEntity<ResponseMoneySourceDto> create(@RequestBody RequestMoneySourceDto requestDto) {
        var created = moneySourceService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление источника
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseMoneySourceDto> update(
            @PathVariable UUID id,
            @RequestBody UpdateMoneySourceDto updateDto) {
        var updated = moneySourceService.update(id, updateDto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    /**
     * Удаление источника
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        moneySourceService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Получение всех источников
     */
    @GetMapping
    public ResponseEntity<List<ResponseMoneySourceDto>> getAll(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String currency

    ) {

        var filterDto = MoneySourceFilterDto.builder().
                id(id)
                .userId(userId)
                .name(name)
                .type(type)
                .currency(currency).build();

        var sources = moneySourceService.findAllWithFilters(filterDto);
        return ResponseEntity.status(HttpStatus.OK).body(sources);
    }
}
