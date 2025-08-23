package com.cedu.controller;

import com.cedu.api.WrapResponseAdvice;
import com.cedu.dto.journal_entry.FilterJournalEntryDto;
import com.cedu.dto.journal_entry.RequestJournalEntryDto;
import com.cedu.dto.journal_entry.ResponseJournalEntryDto;
import com.cedu.dto.journal_entry.UpdateJournalEntryDto;
import com.cedu.enums.JournalEntryStatus;
import com.cedu.service.JournalEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JournalEntryController.class)
@Import(WrapResponseAdvice.class)
public class JournalEntryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JournalEntryService journalEntryService;

    private static final String BASE = "/api/journal";

    @Test
    void create_returnsWrapped201() throws Exception {
        var now = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        var req = RequestJournalEntryDto.builder()
                .userId(UUID.randomUUID())
                .occurredAt(now)
                .bookedAt(now.plusHours(1))
                .description("Created")
                .reversalOfId(null)
                .build();

        var resp = ResponseJournalEntryDto.builder()
                .userId(req.getUserId())
                .occurredAt(req.getOccurredAt())
                .bookedAt(req.getBookedAt())
                .description(req.getDescription())
                .build();

        when(journalEntryService.create(any())).thenReturn(resp);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").value("Created"))
                .andExpect(jsonPath("$.data.user_id").value(req.getUserId().toString()));
    }


    @Test
    void update_returnsWrapped200() throws Exception {
        var id = UUID.randomUUID();
        var now = OffsetDateTime.of(2024, 1, 2, 12, 0, 0, 0, ZoneOffset.UTC);

        var update = UpdateJournalEntryDto.builder()
                .status(JournalEntryStatus.POSTED)
                .build();

        var resp = ResponseJournalEntryDto.builder()
                .status(JournalEntryStatus.POSTED)
                .build();

        when(journalEntryService.update(eq(id), any())).thenReturn(resp);

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("POSTED"));
    }

    @Test
    void delete_returns204() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());

        verify(journalEntryService).delete(id);
    }

    @Test
    void search_withFilter_andPageable_returnsPageResponse() throws Exception {
        var t1 = ResponseJournalEntryDto.builder()
                .description("A").build();
        var t2 = ResponseJournalEntryDto.builder()
                .description("B").build();

        // Клиент просит page=1,size=10, sort=occurredAt,desc
        var pageable = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("occurredAt")));
        // totalElements = 2 — именно это и хотим получить в JSON
        var page = new PageImpl<>(List.of(t1, t2), pageable, 2);

        when(journalEntryService.find(any(FilterJournalEntryDto.class), any(Pageable.class)))
                .thenReturn(page);

        var filter = FilterJournalEntryDto.builder()
                .description("A or B")
                .build();

        mockMvc.perform(post(BASE + "/search")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "occurredAt,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.pagination.total_elements").value(12));

        // Проверим, что контроллер пробросил pageable именно таким, как в запросе
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(journalEntryService).find(any(FilterJournalEntryDto.class), pageableCaptor.capture());
        var passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(1);
        assertThat(passed.getPageSize()).isEqualTo(10);
        assertThat(passed.getSort()).isEqualTo(Sort.by(Sort.Order.desc("occurredAt")));
    }

    @Test
    void search_withoutParams_usesDefaultPageable() throws Exception {
        // Дефолтный @PageableDefault(size=20, page=0, sort=id,DESC)
        var defaultPageable = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("id")));
        var emptyPage = new PageImpl<ResponseJournalEntryDto>(List.of(), defaultPageable, 0);

        when(journalEntryService.find(any(FilterJournalEntryDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(post(BASE + "/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(journalEntryService).find(any(FilterJournalEntryDto.class), pageableCaptor.capture());
        var passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(0);
        assertThat(passed.getPageSize()).isEqualTo(20);
        assertThat(passed.getSort()).isEqualTo(Sort.by(Sort.Order.desc("id")));
    }
}
