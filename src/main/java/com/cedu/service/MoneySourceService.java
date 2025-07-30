package com.cedu.service;

import com.cedu.dto.RequestMoneySourceDTO;
import com.cedu.dto.ResponseMoneySourceDTO;
import com.cedu.dto.UpdateMoneySourceDTO;
import com.cedu.mapper.MoneySourceMapper;
import com.cedu.repository.MoneySourceRepository;
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
    public ResponseMoneySourceDTO create(RequestMoneySourceDTO requestDto) {
        var entity = moneySourceMapper.toEntity(requestDto);
        entity.setId(UUID.randomUUID());
        var saved = moneySourceRepository.save(entity);
        return moneySourceMapper.toDTO(saved);
    }

    /**
     * Обновление существующего источника
     */
    @Transactional
    public ResponseMoneySourceDTO update(UUID id, UpdateMoneySourceDTO requestDto) {
        var existing = moneySourceRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("MoneySource with id=" + id + " not found") );

        moneySourceMapper.updateEntityFromDto(requestDto, existing);

        var updated = moneySourceRepository.save(existing);
        return moneySourceMapper.toDTO(updated);
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
     * Получение всех источников пользователя
     */
    @Transactional(readOnly = true)
    public List<ResponseMoneySourceDTO> findAll() {
        return moneySourceRepository.findAll().stream()
                .map(moneySourceMapper::toDTO)
                .collect(Collectors.toList());
    }

}
