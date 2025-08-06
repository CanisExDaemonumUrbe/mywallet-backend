package com.cedu.mapper;

import com.cedu.dto.tag.RequestTagDto;
import com.cedu.dto.tag.ResponseTagDto;
import com.cedu.dto.tag.UpdateTagDto;
import com.cedu.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    Tag toEntity(RequestTagDto requestTagDto);

    ResponseTagDto toDto(Tag tag);

    void updateEntityFromDto(UpdateTagDto updateTagDto, @MappingTarget Tag tag);
}
