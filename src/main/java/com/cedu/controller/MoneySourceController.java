package com.cedu.controller;

import com.cedu.dto.RequestMoneySourceDTO;
import com.cedu.dto.ResponseMoneySourceDTO;
import com.cedu.dto.UpdateMoneySourceDTO;
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
    public ResponseEntity<ResponseMoneySourceDTO> create(@RequestBody RequestMoneySourceDTO requestDto) {
        var created = moneySourceService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление источника
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseMoneySourceDTO> update(
            @PathVariable UUID id,
            @RequestBody UpdateMoneySourceDTO updateDto) {
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
    @GetMapping("/")
    public ResponseEntity<List<ResponseMoneySourceDTO>> getAll() {
        var sources = moneySourceService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(sources);
    }
}
