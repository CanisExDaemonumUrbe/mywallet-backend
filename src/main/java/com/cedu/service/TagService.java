package com.cedu.service;

import com.cedu.dto.tag.FilterTagDto;
import com.cedu.dto.tag.RequestTagDto;
import com.cedu.dto.tag.ResponseTagDto;
import com.cedu.dto.tag.UpdateTagDto;
import com.cedu.mapper.TagMapper;
import com.cedu.repository.TagRepository;
import com.cedu.specification.TagSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    /**
     * Создание нового тега
     */
    @Transactional
    public ResponseTagDto create(RequestTagDto requestTagDto) {
        var entity = tagMapper.toEntity(requestTagDto);
        var saved = tagRepository.save(entity);
        return tagMapper.toDto(saved);
    }

    /**
     *Обновление существующего тега
     */
    @Transactional
    public ResponseTagDto update(UUID id, UpdateTagDto updateTagDto) {
        var existing = tagRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("Tag with id= "+id+" not found") );

        tagMapper.updateEntityFromDto(updateTagDto, existing);

        var updated = tagRepository.save(existing);
        return tagMapper.toDto(updated);
    }

    /**
     * Удаление тега по id
     */
    @Transactional
    public void delete(UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("Tag with id= "+id+" not found");
        }
        tagRepository.deleteById(id);
    }

    /**
     * Получение тегов по фильтру
     */
    @Transactional(readOnly = true)
    public List<ResponseTagDto> findAllWithFilter(FilterTagDto filterDto) {
        return tagRepository.findAll(TagSpecification.withFilters(filterDto))
                .stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
    }
}
