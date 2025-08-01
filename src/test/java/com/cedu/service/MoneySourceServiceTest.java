package com.cedu.service;

import com.cedu.dto.money_source.RequestMoneySourceDto;
import com.cedu.dto.money_source.ResponseMoneySourceDto;
import com.cedu.dto.money_source.UpdateMoneySourceDto;
import com.cedu.entity.MoneySource;
import com.cedu.mapper.MoneySourceMapper;
import com.cedu.repository.MoneySourceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoneySourceServiceTest {

    @Mock
    private MoneySourceRepository moneySourceRepository;

    @Mock
    private MoneySourceMapper moneySourceMapper;

    @InjectMocks
    private MoneySourceService moneySourceService;

    private RequestMoneySourceDto requestDto;
    private UpdateMoneySourceDto updateDto;
    private MoneySource entity;
    private ResponseMoneySourceDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = RequestMoneySourceDto.builder()
                .name("Test Source")
                .build();

        updateDto = UpdateMoneySourceDto.builder()
                .description("Test Description")
                .build();

        entity = new MoneySource();
        entity.setId(UUID.randomUUID());
        entity.setName("Test Source");

        responseDto = ResponseMoneySourceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    @Test
    void testCreate() {
        when(moneySourceMapper.toEntity(requestDto)).thenReturn(entity);
        when(moneySourceRepository.save(entity)).thenReturn(entity);
        when(moneySourceMapper.toDto(entity)).thenReturn(responseDto);

        var result = moneySourceService.create(requestDto);

        assertThat(result.getId()).isEqualTo(responseDto.getId());
        verify(moneySourceRepository, times(1)).save(any());
        verify(moneySourceMapper).toEntity(requestDto);
        verify(moneySourceMapper).toDto(entity);
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        MoneySource existing = new MoneySource();
        existing.setId(id);
        existing.setName("Old Name");

        when(moneySourceRepository.findById(id)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            UpdateMoneySourceDto dto = invocation.getArgument(0);
            MoneySource target = invocation.getArgument(1);
            target.setDescription(dto.getDescription());
            return null ;
        }).when(moneySourceMapper).updateEntityFromDto(updateDto, existing);
        when(moneySourceRepository.save(existing)).thenReturn(existing);
        when(moneySourceMapper.toDto(existing)).thenReturn(responseDto);

        var result = moneySourceService.update(id, updateDto);

        assertThat(result).isEqualTo(responseDto);
        verify(moneySourceRepository).findById(id);
        verify(moneySourceRepository).save(existing);
        verify(moneySourceMapper).updateEntityFromDto(updateDto, existing);
        verify(moneySourceMapper).toDto(existing);
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();

        when(moneySourceRepository.existsById(id)).thenReturn(true);
        doNothing().when(moneySourceRepository).deleteById(id);

        moneySourceService.delete(id);

        verify(moneySourceRepository).existsById(id);
        verify(moneySourceRepository).deleteById(id);
    }


    @Test
    void testDelete_whenNotExist_thenThrow() {
        UUID id = UUID.randomUUID();

        when(moneySourceRepository.existsById(id)).thenReturn(false);

        Assertions.assertThrows(RuntimeException.class, () -> moneySourceService.delete(id));

        verify(moneySourceRepository).existsById(id);
        verify(moneySourceRepository, never()).deleteById(id);
    }

}
