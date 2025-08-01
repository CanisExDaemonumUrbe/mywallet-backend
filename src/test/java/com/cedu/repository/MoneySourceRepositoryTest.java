package com.cedu.repository;

import com.cedu.dto.money_source.MoneySourceFilterDto;
import com.cedu.entity.MoneySource;
import com.cedu.specification.MoneySourceSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MoneySourceRepositoryTest {

    @Autowired
    private MoneySourceRepository repository;

    private final UUID USER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID USER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.saveAll(List.of(
           new MoneySource(USER_1, "ТБанк", "card", "RUB", "Дебетовая карта"),
           new MoneySource(USER_1, "Сбер", "card", "USD", "Наличные"),
           new MoneySource(USER_2, "Альфа", "deposit", "RUB", "Вклад")
        ));
    }

    @Test
    void testFindAll_noFilters_returnsAll() {
        var filter = MoneySourceFilterDto.builder().build();
        var result = repository.findAll(MoneySourceSpecification.withFilters(filter));

        assertThat(result).hasSize(3);
    }

    @Test
    void testFindByUserId() {
        var filter = MoneySourceFilterDto.builder().userId(USER_1).build();

        var result = repository.findAll(MoneySourceSpecification.withFilters(filter));

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(ms -> ms.getUserId().equals(USER_1));
    }

    @Test
    void testFindByCurrency() {
        var filter = MoneySourceFilterDto.builder().currency("RUB").build();

        var result = repository.findAll(MoneySourceSpecification.withFilters(filter));

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(ms -> ms.getCurrency().equals("RUB"));
    }

    @Test
    void testFindByUserIdAndCurrency() {
        var filter = MoneySourceFilterDto.builder().userId(USER_1).currency("USD").build();

        var result = repository.findAll(MoneySourceSpecification.withFilters(filter));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Сбер");
    }

    @Test
    void testFindByNonexistentCurrency() {
        var filter = MoneySourceFilterDto.builder().userId(USER_2).currency("JPY").build();

        var result = repository.findAll(MoneySourceSpecification.withFilters(filter));

        assertThat(result).isEmpty();
    }
}
