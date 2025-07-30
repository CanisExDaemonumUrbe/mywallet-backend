package com.cedu.mapper;

import com.cedu.dto.RequestMoneySourceDTO;
import com.cedu.dto.ResponseMoneySourceDTO;
import com.cedu.dto.UpdateMoneySourceDTO;
import com.cedu.entity.MoneySource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MoneySourceMapper {

    MoneySource toEntity(RequestMoneySourceDTO dto);

    ResponseMoneySourceDTO toDTO(MoneySource entity);

    void updateEntityFromDto(UpdateMoneySourceDTO dto, @MappingTarget MoneySource entity);

}
