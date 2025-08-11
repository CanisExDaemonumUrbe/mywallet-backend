package com.cedu.service;

import com.cedu.dto.tag.FilterTagDto;
import com.cedu.dto.tag.RequestTagDto;
import com.cedu.dto.tag.ResponseTagDto;
import com.cedu.dto.tag.UpdateTagDto;
import com.cedu.entity.Tag;
import com.cedu.mapper.TagMapper;
import com.cedu.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    private RequestTagDto requestTagDto;
    private UpdateTagDto updateTagDto;
    private Tag entity;
    private ResponseTagDto responseTagDto;

    private final String NAME_1 = "еда";
    private final String NAME_2 = "транспорт";
    private final String NAME_3 = "путешествия";

    @BeforeEach
    void setUp() {
        requestTagDto = RequestTagDto.builder()
                .userId(UUID.randomUUID())
                .name(NAME_1)
                .build();

        updateTagDto = UpdateTagDto.builder()
                .name(NAME_2)
                .build();

        entity = new Tag(UUID.randomUUID(), requestTagDto.getUserId(), requestTagDto.getName());

        responseTagDto = ResponseTagDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();

    }

    @Test
    void testCreateTag() {
        when(tagMapper.toEntity(requestTagDto)).thenReturn(entity);
        when(tagMapper.toDto(entity)).thenReturn(responseTagDto);
        when(tagRepository.save(entity)).thenReturn(entity);

        var result = tagService.create(requestTagDto);

        assertThat(result).isEqualTo(responseTagDto);
        verify(tagRepository).save(entity);
    }

    @Test
    void testUpdateTag() {
        var id = entity.getId();

        when(tagRepository.findById(id)).thenReturn(Optional.of(entity));
        doAnswer(invocation -> {
           UpdateTagDto updateDto = invocation.getArgument(0);
           Tag tag = invocation.getArgument(1);
           tag.setName(updateDto.getName());
           return null;
        }).when(tagMapper).updateEntityFromDto(updateTagDto, entity);

        when(tagRepository.save(entity)).thenReturn(entity);
        when(tagMapper.toDto(entity)).thenReturn(responseTagDto);

        var result = tagService.update(id, updateTagDto);

        assertThat(result).isEqualTo(responseTagDto);
        verify(tagRepository).findById(id);
        verify(tagMapper).updateEntityFromDto(updateTagDto, entity);
        verify(tagRepository).save(entity);
    }

    @Test
    void testDeleteTag() {
        var id = UUID.randomUUID();

        when(tagRepository.existsById(id)).thenReturn(true);

        tagService.delete(id);

        verify(tagRepository).deleteById(id);
    }

    @Test
    void testDelete_whenNotFound() {
        var id = UUID.randomUUID();
        when(tagRepository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> tagService.delete(id));
    }

    @Test
    void testFindAllWithFilter_paged() {
        var filter = FilterTagDto.builder()
                .name(NAME_3)
                .build();

        var tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName(NAME_3);

        Pageable pageable = PageRequest.of(0, 20);

        when(tagRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(tag), pageable, 1));

        when(tagMapper.toDto(any(Tag.class))).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            return ResponseTagDto.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .build();
        });

        Page<ResponseTagDto> result = tagService.findAllWithFilter(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME_3);
    }

}
