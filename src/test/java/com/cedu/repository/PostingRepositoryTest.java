package com.cedu.repository;

import com.cedu.dto.posting.FilterPostingDto;
import com.cedu.entity.Account;
import com.cedu.entity.JournalEntry;
import com.cedu.entity.Posting;
import com.cedu.enums.AccountKind;
import com.cedu.enums.JournalEntryStatus;
import com.cedu.enums.PostingSide;
import com.cedu.specification.PostingSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class PostingRepositoryTest {

    @Autowired
    private PostingRepository repository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    // --- UUID пользователей (их продолжаем фиксировать) ---
    private static final UUID USER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    // --- Суммы ---
    private static final BigDecimal AMOUNT_10   = new BigDecimal("10.00");
    private static final BigDecimal AMOUNT_50_5 = new BigDecimal("50.50");
    private static final BigDecimal AMOUNT_75   = new BigDecimal("75.00");
    private static final BigDecimal AMOUNT_100  = new BigDecimal("100.00");
    private static final BigDecimal AMOUNT_200  = new BigDecimal("200.00");

    // Фактические ID, сгенерированные БД
    private UUID acc1User1Id;
    private UUID acc2User1Id;
    private UUID acc1User2Id;
    private UUID acc2User2Id;

    private UUID je1User1Id;
    private UUID je2User1Id;
    private UUID je1User2Id;
    private UUID je2User2Id;

    private Posting posting(UUID userId, UUID journalEntryId, UUID accountId, PostingSide side, BigDecimal amount) {
        var p = new Posting();
        p.setUserId(userId);
        p.setJournalEntryId(journalEntryId);
        p.setAccountId(accountId);
        p.setSide(side);
        p.setAmount(amount);
        return p;
    }

    private Account acc(UUID id, UUID userId, String name) {
        var a = new Account();
        a.setId(id); // оставляем null при автогенерации
        a.setUserId(userId);
        a.setName(name);
        a.setKind(AccountKind.ASSET);
        a.setIsActive(true);
        return a;
    }

    private JournalEntry je(UUID id, UUID userId) {
        var j = new JournalEntry();
        j.setId(id); // оставляем null при автогенерации
        j.setUserId(userId);
        j.setOccurredAt(OffsetDateTime.now().minusDays(1));
        j.setBookedAt(OffsetDateTime.now());
        j.setStatus(JournalEntryStatus.POSTED);
        j.setDescription("seed");
        return j;
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        journalEntryRepository.deleteAll();
        accountRepository.deleteAll();

        // 1) Сохраняем аккаунты (ID генерятся БД)
        var a1u1 = accountRepository.save(acc(null, USER_1, "A1-U1"));
        var a2u1 = accountRepository.save(acc(null, USER_1, "A2-U1"));
        var a1u2 = accountRepository.save(acc(null, USER_2, "A1-U2"));
        var a2u2 = accountRepository.save(acc(null, USER_2, "A2-U2"));

        acc1User1Id = a1u1.getId();
        acc2User1Id = a2u1.getId();
        acc1User2Id = a1u2.getId();
        acc2User2Id = a2u2.getId();

        // 2) Сохраняем проводки (ID генерятся БД)
        var je1u1 = journalEntryRepository.save(je(null, USER_1));
        var je2u1 = journalEntryRepository.save(je(null, USER_1));
        var je1u2 = journalEntryRepository.save(je(null, USER_2));
        var je2u2 = journalEntryRepository.save(je(null, USER_2));

        je1User1Id = je1u1.getId();
        je2User1Id = je2u1.getId();
        je1User2Id = je1u2.getId();
        je2User2Id = je2u2.getId();

        accountRepository.flush();
        journalEntryRepository.flush();

        // 3) Сохраняем postings, используя реальные (сгенерированные) ID
        repository.saveAll(List.of(
                // USER_1
                posting(USER_1, je1User1Id, acc1User1Id, PostingSide.DEBIT,  AMOUNT_100),
                posting(USER_1, je1User1Id, acc2User1Id, PostingSide.CREDIT, AMOUNT_50_5),
                posting(USER_1, je2User1Id, acc1User1Id, PostingSide.DEBIT,  AMOUNT_200),

                // USER_2
                posting(USER_2, je2User2Id, acc2User2Id, PostingSide.CREDIT, AMOUNT_75),
                posting(USER_2, je1User2Id, acc1User2Id, PostingSide.DEBIT,  AMOUNT_10)
        ));
        repository.flush();
    }

    @Test
    void findAll_noFilters_returnsAll() {
        var filter = FilterPostingDto.builder().build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).hasSize(5);
    }

    @Test
    void findByUserId() {
        var filter = FilterPostingDto.builder()
                .userId(USER_1)
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(p -> p.getUserId().equals(USER_1));
    }

    @Test
    void findByJournalEntryId() {
        // Важное изменение: теперь фильтруем по реальному je1User1Id,
        // а не по фиксированной константе. Ожидание: 2 записи (у USER_1).
        var filter = FilterPostingDto.builder()
                .journalEntryId(je1User1Id)
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getJournalEntryId().equals(je1User1Id));
    }

    @Test
    void findByAmountRange() {
        var filter = FilterPostingDto.builder()
                .amountFrom(new BigDecimal("50.00"))
                .amountTo(new BigDecimal("150.00"))
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).extracting(Posting::getAmount)
                .containsExactlyInAnyOrder(AMOUNT_100, AMOUNT_50_5, AMOUNT_75);
    }

    @Test
    void findByAmountFromOnly() {
        var filter = FilterPostingDto.builder()
                .amountFrom(AMOUNT_100)
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).extracting(Posting::getAmount)
                .containsExactlyInAnyOrder(AMOUNT_100, AMOUNT_200);
    }

    @Test
    void findByAmountToOnly() {
        var filter = FilterPostingDto.builder()
                .amountTo(new BigDecimal("60.00"))
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).extracting(Posting::getAmount)
                .containsExactlyInAnyOrder(AMOUNT_50_5, AMOUNT_10);
    }

    @Test
    void findByCombination_user_account_range() {
        // Раньше фильтровали по фиксированному ACC_1, теперь — по реальному acc1User1Id
        var filter = FilterPostingDto.builder()
                .userId(USER_1)
                .accountId(acc1User1Id)
                .amountFrom(new BigDecimal("50.00"))
                .amountTo(new BigDecimal("150.00"))
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(AMOUNT_100);
    }

    @Test
    void findByNonexistentFilter_returnsEmpty() {
        // у USER_2 & acc1User2Id есть только DEBIT (10.00), значит CREDIT вернёт пусто
        var filter = FilterPostingDto.builder()
                .userId(USER_2)
                .accountId(acc1User2Id)
                .side(PostingSide.CREDIT)
                .build();

        var result = repository.findAll(PostingSpecification.withFilters(filter));

        assertThat(result).isEmpty();
    }
}
