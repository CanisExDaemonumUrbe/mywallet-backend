package com.cedu.controller;

import com.cedu.api.WrapResponseAdvice;
import com.cedu.dto.money_source.ResponseShortMoneySourceDto;
import com.cedu.dto.tag.ResponseTagDto;
import com.cedu.dto.transaction.RequestTransactionDto;
import com.cedu.dto.transaction.ResponseTransactionDto;
import com.cedu.dto.transaction.UpdateTransactionDto;
import com.cedu.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TransactionController.class)
@Import(WrapResponseAdvice.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        Instant date = Instant.parse("2024-01-02T10:15:30Z");

        var request = RequestTransactionDto.builder()
                .userId(userId)
                .date(date)
                .amount(new BigDecimal("123.45"))
                .type("expense")
                .moneySourceId(sourceId)
                .description("Lunch")
                .tagsIds(Set.of(UUID.randomUUID()))
                .build();

        var response = ResponseTransactionDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .date(date)
                .amount(new BigDecimal("123.45"))
                .type("expense")
                .source(ResponseShortMoneySourceDto.builder()
                        .id(sourceId)
                        .name("Card")
                        .build())
                .description("Lunch")
                .tags(Set.of(ResponseTagDto.builder().id(UUID.randomUUID()).name("еда").build()))
                .build();

        when(transactionService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.amount").value(123.45))
                .andExpect(jsonPath("$.data.type").value("expense"))
                .andExpect(jsonPath("$.data.source.id").value(sourceId.toString()))
                .andExpect(jsonPath("$.data.description").value("Lunch"));

        verify(transactionService).create(any());
    }

    @Test
    void update_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID newSourceId = UUID.randomUUID();
        Instant newDate = Instant.parse("2024-03-01T00:00:00Z");

        var update = UpdateTransactionDto.builder()
                .date(newDate)
                .amount(new BigDecimal("200.00"))
                .type("income")
                .sourceId(newSourceId)
                .description("Salary")
                .tagsIds(Set.of())
                .build();

        var response = ResponseTransactionDto.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .date(newDate)
                .amount(new BigDecimal("200.00"))
                .type("income")
                .source(ResponseShortMoneySourceDto.builder().id(newSourceId).name("Card2").build())
                .description("Salary")
                .tags(Set.of())
                .build();

        when(transactionService.update(eq(id), any())).thenReturn(response);

        mockMvc.perform(put("/api/transactions/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.type").value("income"))
                .andExpect(jsonPath("$.data.amount").value(200.00))
                .andExpect(jsonPath("$.data.source.id").value(newSourceId.toString()))
                .andExpect(jsonPath("$.data.description").value("Salary"));

        verify(transactionService).update(eq(id), any());
    }

    @Test
    void delete_ok() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/transactions/" + id))
                .andExpect(status().isNoContent());

        verify(transactionService).delete(id);
    }

    @Test
    void getAll_withFilters_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        var tx = ResponseTransactionDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .date(Instant.parse("2024-01-02T10:15:30Z"))
                .amount(new BigDecimal("50.00"))
                .type("expense")
                .source(ResponseShortMoneySourceDto.builder().id(sourceId).name("Card").build())
                .description("Food")
                .tags(Set.of(ResponseTagDto.builder().id(tagId).name("еда").build()))
                .build();

        var page = new PageImpl<>(List.of(tx), PageRequest.of(0, 20), 1);

        when(transactionService.findAllWithFilter(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions")
                        .param("userId", userId.toString())
                        .param("type", "expense")
                        .param("sourceId", sourceId.toString())
                        .param("tagsId", tagId.toString())
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-01-31T23:59:59Z")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.data[0].type").value("expense"))
                .andExpect(jsonPath("$.data[0].source.id").value(sourceId.toString()))
                .andExpect(jsonPath("$.data[0].tags[0].id").value(tagId.toString()))
                .andExpect(jsonPath("$.pagination.totalElements").value(1))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20));

        verify(transactionService).findAllWithFilter(any(), any(Pageable.class));
    }

    @Test
    void getAll_noFilters_ok() throws Exception {
        var emptyPage = new PageImpl<ResponseTransactionDto>(List.of(), PageRequest.of(0, 20), 0);

        when(transactionService.findAllWithFilter(any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20));

        verify(transactionService).findAllWithFilter(any(), any(Pageable.class));
    }
}
