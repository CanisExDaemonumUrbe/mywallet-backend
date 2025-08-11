package com.cedu.mapper;

import com.cedu.dto.money_source.RequestMoneySourceDto;
import com.cedu.dto.money_source.ResponseFullMoneySourceDto;
import com.cedu.dto.money_source.ResponseShortMoneySourceDto;
import com.cedu.dto.money_source.UpdateMoneySourceDto;
import com.cedu.entity.MoneySource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MoneySourceMapper {

    MoneySource toEntity(RequestMoneySourceDto dto);

    ResponseFullMoneySourceDto toFullDto(MoneySource entity);

    ResponseShortMoneySourceDto toShortDto(MoneySource entity);

    void updateEntityFromDto(UpdateMoneySourceDto dto, @MappingTarget MoneySource entity);

}
