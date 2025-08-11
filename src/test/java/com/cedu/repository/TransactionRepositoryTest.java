package com.cedu.repository;

import com.cedu.dto.transaction.FilterTransactionDto;
import com.cedu.entity.MoneySource;
import com.cedu.entity.Tag;
import com.cedu.entity.Transaction;
import com.cedu.specification.TransactionSpecification;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager em;

    private UUID USER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private UUID USER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private MoneySource src1_user1; // RUB, "card"
    private MoneySource src2_user1; // USD, "cash"
    private MoneySource src1_user2; // RUB, "card"

    private Tag tagFood;
    private Tag tagTravel;
    private Tag tagTaxi;

    private Transaction t1_u1_food_src1;   // 2024-01-02, expense, RUB
    private Transaction t2_u1_travel_src2; // 2024-01-15, expense, USD
    private Transaction t3_u2_taxi_src3;   // 2024-02-01, expense, RUB
    private Transaction t4_u1_income_src1; // 2024-02-10, income, RUB

    @BeforeEach
    void setUp() {
        // Money sources
        src1_user1 = new MoneySource();
        src1_user1.setUserId(USER_1);
        src1_user1.setName("Card TBank");
        src1_user1.setType("card");
        src1_user1.setCurrency("RUB");
        src1_user1.setDescription("Main");
        em.persist(src1_user1);

        src2_user1 = new MoneySource();
        src2_user1.setUserId(USER_1);
        src2_user1.setName("Cash");
        src2_user1.setType("cash");
        src2_user1.setCurrency("USD");
        src2_user1.setDescription("Wallet");
        em.persist(src2_user1);

        src1_user2 = new MoneySource();
        src1_user2.setUserId(USER_2);
        src1_user2.setName("Card Sber");
        src1_user2.setType("card");
        src1_user2.setCurrency("RUB");
        src1_user2.setDescription("Debit");
        em.persist(src1_user2);

        // Tags
        tagFood = new Tag();
        tagFood.setUserId(USER_1);
        tagFood.setName("еда");
        em.persist(tagFood);

        tagTravel = new Tag();
        tagTravel.setUserId(USER_1);
        tagTravel.setName("путешествия");
        em.persist(tagTravel);

        tagTaxi = new Tag();
        tagTaxi.setUserId(USER_2);
        tagTaxi.setName("такси");
        em.persist(tagTaxi);

        // Transactions
        t1_u1_food_src1 = new Transaction();
        t1_u1_food_src1.setUserId(USER_1);
        t1_u1_food_src1.setDate(Instant.parse("2024-01-02T10:00:00Z"));
        t1_u1_food_src1.setAmount(new BigDecimal("123.45"));
        t1_u1_food_src1.setType("expense");
        t1_u1_food_src1.setSource(src1_user1);
        t1_u1_food_src1.setDescription("Lunch");
        t1_u1_food_src1.setTags(new LinkedHashSet<>(Set.of(tagFood)));
        em.persist(t1_u1_food_src1);

        t2_u1_travel_src2 = new Transaction();
        t2_u1_travel_src2.setUserId(USER_1);
        t2_u1_travel_src2.setDate(Instant.parse("2024-01-15T12:00:00Z"));
        t2_u1_travel_src2.setAmount(new BigDecimal("999.99"));
        t2_u1_travel_src2.setType("expense");
        t2_u1_travel_src2.setSource(src2_user1);
        t2_u1_travel_src2.setDescription("Tickets");
        t2_u1_travel_src2.setTags(new LinkedHashSet<>(Set.of(tagTravel)));
        em.persist(t2_u1_travel_src2);

        t3_u2_taxi_src3 = new Transaction();
        t3_u2_taxi_src3.setUserId(USER_2);
        t3_u2_taxi_src3.setDate(Instant.parse("2024-02-01T08:30:00Z"));
        t3_u2_taxi_src3.setAmount(new BigDecimal("300.00"));
        t3_u2_taxi_src3.setType("expense");
        t3_u2_taxi_src3.setSource(src1_user2);
        t3_u2_taxi_src3.setDescription("Taxi");
        t3_u2_taxi_src3.setTags(new LinkedHashSet<>(Set.of(tagTaxi)));
        em.persist(t3_u2_taxi_src3);

        t4_u1_income_src1 = new Transaction();
        t4_u1_income_src1.setUserId(USER_1);
        t4_u1_income_src1.setDate(Instant.parse("2024-02-10T09:00:00Z"));
        t4_u1_income_src1.setAmount(new BigDecimal("5000.00"));
        t4_u1_income_src1.setType("income");
        t4_u1_income_src1.setSource(src1_user1);
        t4_u1_income_src1.setDescription("Salary");
        t4_u1_income_src1.setTags(new LinkedHashSet<>());
        em.persist(t4_u1_income_src1);

        em.flush();
        em.clear();
    }

    @Test
    void findAll_noFilters_returnsAll() {
        var filter = FilterTransactionDto.builder().build();
        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).hasSize(4);
    }

    @Test
    void findByUserId_onlyUser1() {
        var filter = FilterTransactionDto.builder().userId(USER_1).build();
        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(tx -> USER_1.equals(tx.getUserId()));
    }

    @Test
    void findByDateRange_inJanuaryOnly() {
        var filter = FilterTransactionDto.builder()
                .from(Instant.parse("2024-01-01T00:00:00Z"))
                .to(Instant.parse("2024-01-31T23:59:59Z"))
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        // т1 (2024-01-02) и т2 (2024-01-15) попадают
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(t1_u1_food_src1.getId(), t2_u1_travel_src2.getId());
    }

    @Test
    void findFrom_only() {
        var filter = FilterTransactionDto.builder()
                .from(Instant.parse("2024-02-01T00:00:00Z")) // включает 2024-02
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(t3_u2_taxi_src3.getId(), t4_u1_income_src1.getId());
    }

    @Test
    void findTo_only() {
        var filter = FilterTransactionDto.builder()
                .to(Instant.parse("2024-01-31T23:59:59Z"))
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(t1_u1_food_src1.getId(), t2_u1_travel_src2.getId());
    }

    @Test
    void findByType_expense() {
        var filter = FilterTransactionDto.builder().type("expense").build();
        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(tx -> "expense".equals(tx.getType()));
    }

    @Test
    void findBySourceId_src1_user1() {
        var filter = FilterTransactionDto.builder().sourceId(src1_user1.getId()).build();
        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(t1_u1_food_src1.getId(), t4_u1_income_src1.getId());
    }

    @Test
    void findBySingleTag_food() {
        var filter = FilterTransactionDto.builder()
                .tagsId(Set.of(tagFood.getId()))
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(t1_u1_food_src1.getId());
    }

    @Test
    void findByMultipleTags_foodOrTravel_distinct() {
        var filter = FilterTransactionDto.builder()
                .tagsId(Set.of(tagFood.getId(), tagTravel.getId()))
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        // должны прийти т1 (food) и т2 (travel)
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(t1_u1_food_src1.getId(), t2_u1_travel_src2.getId());
        // distinct(true) в спеках должен устранять дубликаты при join
        assertThat(result).doesNotHaveDuplicates();
    }

    @Test
    void combined_user1_january_expense_bySource2() {
        var filter = FilterTransactionDto.builder()
                .userId(USER_1)
                .from(Instant.parse("2024-01-01T00:00:00Z"))
                .to(Instant.parse("2024-01-31T23:59:59Z"))
                .type("expense")
                .sourceId(src2_user1.getId())
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        // должен остаться только t2_u1_travel_src2
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(t2_u1_travel_src2.getId());
    }

    @Test
    void tagNotFound_returnsEmpty() {
        var randomTag = UUID.fromString("00000000-0000-0000-0000-00000000ABCD");
        var filter = FilterTransactionDto.builder()
                .tagsId(Set.of(randomTag))
                .build();

        var result = transactionRepository.findAll(TransactionSpecification.withFilters(filter));
        assertThat(result).isEmpty();
    }
}
