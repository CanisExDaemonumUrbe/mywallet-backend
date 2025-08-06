package com.cedu.repository;

import com.cedu.dto.tag.FilterTagDto;
import com.cedu.entity.Tag;
import com.cedu.specification.TagSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    private final UUID USER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID USER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final String NAME_1 = "еда";
    private final String NAME_2 = "транспорт";
    private final String NAME_3 = "путешествия";

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();

        tagRepository.saveAll(List.of(
                createTag(USER_1, NAME_1),
                createTag(USER_1, NAME_2),
                createTag(USER_2, NAME_3)
        ));
    }

    private Tag createTag(UUID userId, String name) {
        var tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        return tag;
    }

    @Test
    void testFindAll_noFilters() {
        var result = tagRepository.findAll(TagSpecification.withFilters(FilterTagDto.builder().build()));
        assertThat(result).hasSize(3);
    }

    @Test
    void testFindByUserId() {
        var result = tagRepository.findAll(
                TagSpecification.withFilters(FilterTagDto.builder().userId(USER_1).build())
        );

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(tag -> tag.getUserId().equals(USER_1));
    }

    @Test
    void testFindByName() {

        var result = tagRepository.findAll(
                TagSpecification.withFilters(FilterTagDto.builder().name(NAME_1).build())
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(NAME_1);
    }

    @Test
    void testFindByUserIdAndName() {
        var result = tagRepository.findAll(
                TagSpecification.withFilters(
                        FilterTagDto.builder()
                                .userId(USER_1)
                                .name(NAME_2)
                                .build())
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(NAME_2);
    }

    @Test
    void testFindByNonexistentTag() {
        var result = tagRepository.findAll(
                TagSpecification.withFilters(FilterTagDto.builder().name("нет_такого_тэга").build())
        );

        assertThat(result).isEmpty();
    }
}
