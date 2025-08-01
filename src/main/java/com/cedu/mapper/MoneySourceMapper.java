package com.cedu.mapper;

import com.cedu.dto.money_source.RequestMoneySourceDto;
import com.cedu.dto.money_source.ResponseMoneySourceDto;
import com.cedu.dto.money_source.UpdateMoneySourceDto;
import com.cedu.entity.MoneySource;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MoneySourceMapper {

    MoneySource toEntity(RequestMoneySourceDto dto);

    ResponseMoneySourceDto toDto(MoneySource entity);

    void updateEntityFromDto(UpdateMoneySourceDto dto, @MappingTarget MoneySource entity);

}
