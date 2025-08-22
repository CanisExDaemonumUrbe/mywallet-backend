package com.cedu.mapper;

import com.cedu.dto.account.RequestAccountDto;
import com.cedu.dto.account.ResponseAccountDto;
import com.cedu.dto.account.UpdateAccountDto;
import com.cedu.entity.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Account toEntity(RequestAccountDto requestDto);

    ResponseAccountDto toDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateAccountDto updateDto, @MappingTarget Account account);
}
