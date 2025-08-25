package com.cedu.controller;

import com.cedu.api.WrapResponseAdvice;
import com.cedu.dto.posting.FilterPostingDto;
import com.cedu.dto.posting.RequestPostingDto;
import com.cedu.dto.posting.ResponsePostingDto;
import com.cedu.enums.PostingSide;
import com.cedu.service.PostingService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostingController.class)
@Import(WrapResponseAdvice.class)
public class PostingControllerTest {

    private static final String BASE = "/api/postings";
    private static final BigDecimal AMOUNT_123_45 = new BigDecimal("123.45");
    private static final PostingSide DEBIT = PostingSide.DEBIT;
    private static final PostingSide CREDIT = PostingSide.CREDIT;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostingService postingService;

    @Test
    void create_returnsWrapped201() throws Exception {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var journalEntryId = UUID.randomUUID();

        var req = RequestPostingDto.builder()
                .userId(userId)
                .accountId(accountId)
                .journalEntryId(journalEntryId)
                .side(DEBIT)
                .amount(AMOUNT_123_45)
                .build();

        var resp = ResponsePostingDto.builder()
                .id(id)
                .userId(userId)
                .accountId(accountId)
                .journalEntryId(journalEntryId)
                .side(DEBIT)
                .amount(AMOUNT_123_45)
                .build();

        when(postingService.create(any())).thenReturn(resp);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.data.account_id").value(accountId.toString()))
                .andExpect(jsonPath("$.data.journal_entry_id").value(journalEntryId.toString()))
                .andExpect(jsonPath("$.data.side").value(DEBIT.toString()))
                .andExpect(jsonPath("$.data.amount").value(AMOUNT_123_45.toString()));
    }

    @Test
    void delete_returns204() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());

        verify(postingService).delete(id);
    }

    @Test
    void search_withFilter_andPageable_returnsPageResponse() throws Exception {
        // Клиент просит page=1,size=10, sort=id,desc
        var pageable = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("id")));

        var p1 = ResponsePostingDto.builder()
                .id(UUID.randomUUID())
                .side(DEBIT)
                .amount(AMOUNT_123_45)
                .build();

        var p2 = ResponsePostingDto.builder()
                .id(UUID.randomUUID())
                .side(CREDIT)
                .amount(new BigDecimal("10.00"))
                .build();

        // totalElements в PageImpl ставим 2, но WrapResponseAdvice считает total_elements как page*size + content.size = 1*10 + 2 = 12
        var page = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(postingService.find(any(FilterPostingDto.class), any(Pageable.class)))
                .thenReturn(page);

        var filter = FilterPostingDto.builder()
                .build();

        mockMvc.perform(post(BASE + "/search")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.pagination.total_elements").value(12));

        // Проверим, что контроллер пробросил pageable именно таким, как в запросе
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postingService).find(any(FilterPostingDto.class), pageableCaptor.capture());
        var passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(1);
        assertThat(passed.getPageSize()).isEqualTo(10);
        assertThat(passed.getSort()).isEqualTo(Sort.by(Sort.Order.desc("id")));
    }

    @Test
    void search_withoutParams_usesDefaultPageable() throws Exception {
        // Дефолтный @PageableDefault(size=20, page=0, sort=id,DESC) из PostingController
        var defaultPageable = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("id")));
        var emptyPage = new PageImpl<ResponsePostingDto>(List.of(), defaultPageable, 0);

        when(postingService.find(any(FilterPostingDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(post(BASE + "/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postingService).find(any(FilterPostingDto.class), pageableCaptor.capture());
        var passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(0);
        assertThat(passed.getPageSize()).isEqualTo(20);
        assertThat(passed.getSort()).isEqualTo(Sort.by(Sort.Order.desc("id")));
    }
}
