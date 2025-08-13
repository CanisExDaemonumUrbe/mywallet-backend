package com.cedu.controller;

import com.cedu.dto.money_source.RequestMoneySourceDto;
import com.cedu.dto.money_source.ResponseFullMoneySourceDto;
import com.cedu.dto.money_source.UpdateMoneySourceDto;
import com.cedu.service.MoneySourceService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MoneySourceController.class)
@Import(com.cedu.api.WrapResponseAdvice.class)
public class MoneySourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MoneySourceService moneySourceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreate() throws Exception {
        var request = RequestMoneySourceDto.builder()
                .userId(UUID.randomUUID())
                .name("ТБанк")
                .type("card")
                .currency("RUB")
                .description("Main")
                .build();

        var response = ResponseFullMoneySourceDto.builder()
                .id(UUID.randomUUID())
                .userId(request.getUserId())
                .name(request.getName())
                .type(request.getType())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .build();


        when(moneySourceService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value(request.getName()));

    }

    @Test
    void testUpdate() throws Exception {
        UUID id = UUID.randomUUID();

        var update = UpdateMoneySourceDto.builder()
                .description("Update")
                .build();

        var response = ResponseFullMoneySourceDto.builder()
                .id(id)
                .description(update.getDescription())
                .build();

        when(moneySourceService.update(eq(id), any())).thenReturn(response);

        mockMvc.perform(put("/api/sources/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value(update.getDescription()));

    }

    @Test
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/sources/" + id))
                .andExpect(status().isNoContent());

        verify(moneySourceService).delete(id);
    }

    @Test
    void testGetAll_withFilters() throws Exception {
        UUID userId = UUID.randomUUID();

        var response = ResponseFullMoneySourceDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("ТБанк")
                .type("card")
                .currency("RUB")
                .description("Main")
                .build();

        var page = new PageImpl<>(List.of(response),
                PageRequest.of(0, 20), 1);

        when(moneySourceService.findAllWithFilters(any(),
                any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/sources")
                        .param("userId", userId.toString())
                        .param("currency", "RUB")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value(response.getName()))
                .andExpect(jsonPath("$.data[0].currency").value(response.getCurrency()))
                .andExpect(jsonPath("$.pagination.totalElements").value(1))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20));

    }

    @Test
    void testGetAll_noFilters() throws Exception {
        var response = ResponseFullMoneySourceDto.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("Cash")
                .type("cash")
                .currency("USD")
                .description("Some cash")
                .build();

        var page = new PageImpl<>(List.of(response),
                PageRequest.of(0, 20), 1);

        when(moneySourceService.findAllWithFilters(any(),
                any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/sources")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value(response.getType()))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));

    }
}
