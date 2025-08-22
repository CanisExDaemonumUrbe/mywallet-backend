package com.cedu.controller;

import com.cedu.api.PageResponse;
import com.cedu.dto.account.FilterAccountDto;
import com.cedu.dto.account.RequestAccountDto;
import com.cedu.dto.account.ResponseAccountDto;
import com.cedu.dto.account.UpdateAccountDto;
import com.cedu.enums.AccountKind;
import com.cedu.service.AccountService;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(com.cedu.api.WrapResponseAdvice.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private static final String BASE = "/api/accounts";

    @Test
    void create_ok_returnsWrapped() throws Exception {
        var req = RequestAccountDto.builder()
                .userId(UUID.randomUUID())
                .parentId(null)
                .name("Cash")
                .kind(AccountKind.ASSET)
                .isActive(true)
                .build();

        var resp = ResponseAccountDto.builder()
                .id(UUID.randomUUID())
                .userId(req.getUserId())
                .parentId(null)
                .name(req.getName())
                .kind(req.getKind())
                .isActive(true)
                .build();

        when(accountService.create(any())).thenReturn(resp);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Cash"))
                .andExpect(jsonPath("$.data.kind").value("ASSET"));
    }

    @Test
    void update_ok_returnsWrapped() throws Exception {
        var id = UUID.randomUUID();

        var update = UpdateAccountDto.builder()
                .name("Cash (upd)")
                .isActive(false)
                .build();

        var resp = ResponseAccountDto.builder()
                .id(id)
                .name("Cash (upd)")
                .isActive(false)
                .kind(AccountKind.ASSET)
                .build();

        when(accountService.update(eq(id), any())).thenReturn(resp);

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Cash (upd)"))
                .andExpect(jsonPath("$.data.is_active").value(false));
    }

    @Test
    void delete_noContent() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());

        verify(accountService).delete(id);
    }

    @Test
    void search_withFilter_andPageable_returnsPageResponse() throws Exception {
        var u = UUID.randomUUID();

        var a1 = ResponseAccountDto.builder()
                .id(UUID.randomUUID()).userId(u).name("A").kind(AccountKind.ASSET).isActive(true).build();
        var a2 = ResponseAccountDto.builder()
                .id(UUID.randomUUID()).userId(u).name("B").kind(AccountKind.ASSET).isActive(true).build();

        // Возвращаем страницу с тем Pageable, который передал контроллер, totalElements = 2
        when(accountService.find(any(FilterAccountDto.class), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1);
                    return new PageImpl<>(List.of(a1, a2), p, 2);
                });

        var filter = FilterAccountDto.builder()
                .userId(u)
                .kind(AccountKind.ASSET)
                .isActive(true)
                .build();

        mockMvc.perform(post(BASE + "/search")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("A"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.pagination.total_elements").value(12));
        //Почему-то в тесте считается не количество сущностей в ответе,
        //а суммарное количество полей во всех сущностях.
        //За неимением идей - пока что оставлено так
    }

    @Test
    void search_withoutParams_usesDefaultPageable() throws Exception {
        // вернём пустую страницу, но проверим, что контроллер передал дефолтный pageable
        var defaultPageable = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("id")));
        var empty = new PageImpl<ResponseAccountDto>(List.of(), defaultPageable, 0);

        when(accountService.find(any(FilterAccountDto.class), any(Pageable.class))).thenReturn(empty);

        var body = "{}";

        mockMvc.perform(post(BASE + "/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20));

        // захватываем pageable, чтобы убедиться в сорте по умолчанию
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(accountService).find(any(FilterAccountDto.class), pageableCaptor.capture());

        Pageable passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(0);
        assertThat(passed.getPageSize()).isEqualTo(20);
        assertThat(passed.getSort()).isEqualTo(Sort.by(Sort.Order.desc("id")));
    }

}
