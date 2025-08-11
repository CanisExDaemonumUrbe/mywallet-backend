package com.cedu.mapper;

import com.cedu.dto.transaction.RequestTransactionDto;
import com.cedu.dto.transaction.ResponseTransactionDto;
import com.cedu.dto.transaction.UpdateTransactionDto;
import com.cedu.entity.Transaction;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {TagMapper.class, MoneySourceMapper.class}
)
public interface TransactionMapper {

    @Mapping(target = "source", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Transaction toEntity(RequestTransactionDto requestTransactionDto);

    ResponseTransactionDto toDto(Transaction transaction);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntity(UpdateTransactionDto updateTransactionDto, @MappingTarget Transaction transaction);
}
