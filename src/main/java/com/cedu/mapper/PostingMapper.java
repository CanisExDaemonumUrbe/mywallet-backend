package com.cedu.mapper;

import com.cedu.dto.posting.RequestPostingDto;
import com.cedu.dto.posting.ResponsePostingDto;
import com.cedu.entity.Posting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostingMapper {

    @Mapping(target = "journalEntry", ignore = true)
    @Mapping(target = "account", ignore = true)
    Posting toEntity(RequestPostingDto requestDto);

    ResponsePostingDto toDto(Posting posting);
}
