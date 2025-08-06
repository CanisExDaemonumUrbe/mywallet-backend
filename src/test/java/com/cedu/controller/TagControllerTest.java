package com.cedu.controller;

import com.cedu.dto.tag.RequestTagDto;
import com.cedu.dto.tag.ResponseTagDto;
import com.cedu.dto.tag.UpdateTagDto;
import com.cedu.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(TagController.class)
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String NAME_1 = "еда";
    private final String NAME_2 = "транспорт";
    private final String NAME_3 = "путешествия";

    private final String BASE_URL = "/api/tags";

    @Test
    void testCreate() throws Exception {
        var request = RequestTagDto.builder()
                .userId(UUID.randomUUID())
                .name(NAME_1)
                .build();

        var response = ResponseTagDto.builder()
                .id(UUID.randomUUID())
                .name(NAME_1)
                .build();

        when(tagService.create(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(NAME_1));
    }

    @Test
    void testUpdate() throws Exception {
        var id = UUID.randomUUID();
        var update = UpdateTagDto.builder().name(NAME_2).build();
        var response = ResponseTagDto.builder().id(id).name(NAME_2).build();

        when(tagService.update(eq(id), any())).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NAME_2));
    }

    @Test
    void testDelete() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        verify(tagService).delete(id);
    }

    @Test
    void testGetAll_WithFilters() throws Exception {
        var tag = ResponseTagDto.builder()
                .id(UUID.randomUUID())
                .name(NAME_3)
                .build();

        when(tagService.findAllWithFilter(any())).thenReturn(List.of(tag));

        mockMvc.perform(get(BASE_URL)
                .param("name", NAME_3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(NAME_3));
    }
}
