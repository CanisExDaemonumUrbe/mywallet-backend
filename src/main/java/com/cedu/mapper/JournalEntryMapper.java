package com.cedu.mapper;

import com.cedu.dto.journal_entry.RequestJournalEntryDto;
import com.cedu.dto.journal_entry.ResponseJournalEntryDto;
import com.cedu.dto.journal_entry.UpdateJournalEntryDto;
import com.cedu.entity.JournalEntry;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JournalEntryMapper {

    @Mapping(target = "reversalOfId", ignore = true)
    @Mapping(target = "reversalOf", ignore = true)
    JournalEntry toEntity(RequestJournalEntryDto requestDto);

    ResponseJournalEntryDto toDto(JournalEntry journalEntry);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateJournalEntryDto updateDto, @MappingTarget JournalEntry journalEntry);
}
