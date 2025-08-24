package com.cedu.repository;

import com.cedu.dto.journal_entry.FilterJournalEntryDto;
import com.cedu.entity.JournalEntry;
import com.cedu.enums.JournalEntryStatus;
import com.cedu.specification.JournalEntrySpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class JournalEntryTest {

    @Autowired
    private JournalEntryRepository repository;

    private final UUID USER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID USER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    // Фиксированная «сейчас», чтобы тесты были детерминированными
    private final OffsetDateTime T0 = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    private JournalEntry je1_user1_posted;   // occurred: T0,     booked: T0+1
    private JournalEntry je2_user1_draft;    // occurred: T0+10,  booked: T0+11
    private JournalEntry je3_user2_posted;   // occurred: T0+20,  booked: T0+21
    private JournalEntry je4_user1_posted_revOf_je2; // reversal of je2

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        // je1
        je1_user1_posted = new JournalEntry();
        je1_user1_posted.setUserId(USER_1);
        je1_user1_posted.setOccurredAt(T0);
        je1_user1_posted.setBookedAt(T0.plusDays(1));
        je1_user1_posted.setDescription("Salary January");
        je1_user1_posted.setStatus(JournalEntryStatus.POSTED);
        je1_user1_posted = repository.save(je1_user1_posted);

        // je2
        je2_user1_draft = new JournalEntry();
        je2_user1_draft.setUserId(USER_1);
        je2_user1_draft.setOccurredAt(T0.plusDays(10));
        je2_user1_draft.setBookedAt(T0.plusDays(11));
        je2_user1_draft.setDescription("Groceries");
        je2_user1_draft.setStatus(JournalEntryStatus.VOID);
        je2_user1_draft = repository.save(je2_user1_draft);

        // je3
        je3_user2_posted = new JournalEntry();
        je3_user2_posted.setUserId(USER_2);
        je3_user2_posted.setOccurredAt(T0.plusDays(20));
        je3_user2_posted.setBookedAt(T0.plusDays(21));
        je3_user2_posted.setDescription("Refund");
        je3_user2_posted.setStatus(JournalEntryStatus.POSTED);
        je3_user2_posted = repository.save(je3_user2_posted);

        // je4 (reversal of je2)
        je4_user1_posted_revOf_je2 = new JournalEntry();
        je4_user1_posted_revOf_je2.setUserId(USER_1);
        je4_user1_posted_revOf_je2.setOccurredAt(T0.plusDays(15));
        je4_user1_posted_revOf_je2.setBookedAt(T0.plusDays(16));
        je4_user1_posted_revOf_je2.setDescription("Reversal groceries");
        je4_user1_posted_revOf_je2.setStatus(JournalEntryStatus.POSTED);
        // связь по полям (insertable/updatable=false) — выставляем ID
        je4_user1_posted_revOf_je2.setReversalOf(je2_user1_draft);
        je4_user1_posted_revOf_je2 = repository.save(je4_user1_posted_revOf_je2);
    }

    @Test
    void findAll_noFilters_returnsAll() {
        var filter = FilterJournalEntryDto.builder().build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        assertThat(result).hasSize(4);
    }

    @Test
    void filterByUserId() {
        var filter = FilterJournalEntryDto.builder()
                .userId(USER_1)
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(e -> USER_1.equals(e.getUserId()));
    }

    @Test
    void filterByOccurredBetween() {
        var filter = FilterJournalEntryDto.builder()
                .occurredFrom(T0.plusDays(5))
                .occurredTo(T0.plusDays(18))
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        // В окно попадают: je2 (T0+10), je4 (T0+15)
        assertThat(result).extracting(JournalEntry::getId)
                .containsExactlyInAnyOrder(je2_user1_draft.getId(), je4_user1_posted_revOf_je2.getId());
    }

    @Test
    void filterByBookedBetween() {
        var filter = FilterJournalEntryDto.builder()
                .bookedFrom(T0.plusDays(21))
                .bookedTo(T0.plusDays(22))
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        assertThat(result).extracting(JournalEntry::getId)
                .containsExactlyInAnyOrder(je3_user2_posted.getId());
    }

    @Test
    void filterByDescription_like() {
        var filter = FilterJournalEntryDto.builder()
                .description("grocer") // подстрока "Groceries"
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        assertThat(result).extracting(JournalEntry::getId)
                .containsExactlyInAnyOrder(je2_user1_draft.getId(), je4_user1_posted_revOf_je2.getId());
    }

    @Test
    void filterByStatus() {
        var filter = FilterJournalEntryDto.builder()
                .status(JournalEntryStatus.POSTED)
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        assertThat(result).extracting(JournalEntry::getId)
                .containsExactlyInAnyOrder(
                        je1_user1_posted.getId(),
                        je3_user2_posted.getId(),
                        je4_user1_posted_revOf_je2.getId()
                );
    }

    @Test
    void filterByReversalOfId() {
        var filter = FilterJournalEntryDto.builder()
                .reversalOfId(je2_user1_draft.getId())
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        assertThat(result).extracting(JournalEntry::getId)
                .containsExactlyInAnyOrder(je4_user1_posted_revOf_je2.getId());
    }

    @Test
    void filterCombined_userAndStatusAndOccurredRange() {
        var filter = FilterJournalEntryDto.builder()
                .userId(USER_1)
                .status(JournalEntryStatus.POSTED)
                .occurredFrom(T0.plusDays(14))
                .occurredTo(T0.plusDays(16))
                .build();

        var result = repository.findAll(JournalEntrySpecification.withFilters(filter));

        // Должен найтись только je4 (user1, posted, occurred T0+15)
        assertThat(result).extracting(JournalEntry::getId)
                .containsExactly(je4_user1_posted_revOf_je2.getId());
    }

    @Test
    void prePersist_setsBookedAt_ifNull() {
        var e = new JournalEntry();
        e.setUserId(USER_1);
        e.setOccurredAt(T0.plusDays(30));
        e.setDescription("Auto-booked");
        e.setStatus(JournalEntryStatus.VOID);
        e.setBookedAt(null); // оставляем null — должен установиться в prePersist

        var saved = repository.save(e);

        assertThat(saved.getBookedAt()).isNotNull();
    }

}
