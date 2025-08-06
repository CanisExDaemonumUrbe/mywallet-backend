package com.cedu.service;

import com.cedu.dto.money_source.FilterMoneySourceDto;
import com.cedu.dto.money_source.RequestMoneySourceDto;
import com.cedu.dto.money_source.ResponseMoneySourceDto;
import com.cedu.dto.money_source.UpdateMoneySourceDto;
import com.cedu.mapper.MoneySourceMapper;
import com.cedu.repository.MoneySourceRepository;
import com.cedu.specification.MoneySourceSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MoneySourceService {

    private final MoneySourceRepository moneySourceRepository;
    private final MoneySourceMapper moneySourceMapper;

    public MoneySourceService(MoneySourceRepository moneySourceRepository, MoneySourceMapper moneySourceMapper) {
        this.moneySourceRepository = moneySourceRepository;
        this.moneySourceMapper = moneySourceMapper;
    }

    /**
     * Создание нового источника
     */
    @Transactional
    public ResponseMoneySourceDto create(RequestMoneySourceDto requestDto) {
        var entity = moneySourceMapper.toEntity(requestDto);
        var saved = moneySourceRepository.save(entity);
        return moneySourceMapper.toDto(saved);
    }

    /**
     * Обновление существующего источника
     */
    @Transactional
    public ResponseMoneySourceDto update(UUID id, UpdateMoneySourceDto requestDto) {
        var existing = moneySourceRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("MoneySource with id=" + id + " not found") );

        moneySourceMapper.updateEntityFromDto(requestDto, existing);

        var updated = moneySourceRepository.save(existing);
        return moneySourceMapper.toDto(updated);
    }

    /**
     * Удаление источника по ID
     */
    @Transactional
    public void delete(UUID id) {
        if (!moneySourceRepository.existsById(id)) {
            throw new RuntimeException("MoneySource with id=" + id + " not found");
        }
        moneySourceRepository.deleteById(id);
    }

    /**
     * Получение источников по фильтру
     */
    @Transactional(readOnly = true)
    public List<ResponseMoneySourceDto> findAllWithFilters(FilterMoneySourceDto filterDto) {
        return moneySourceRepository.findAll(MoneySourceSpecification.withFilters(filterDto))
                .stream()
                .map(moneySourceMapper::toDto)
                .collect(Collectors.toList());
    }

}
