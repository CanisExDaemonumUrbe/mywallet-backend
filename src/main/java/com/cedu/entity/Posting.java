package com.cedu.entity;

import com.cedu.enums.PostingSide;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "posting")
@Getter @Setter
public class Posting {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "journal_entry_id", nullable = false)
    private UUID journalEntryId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private PostingSide side;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "journal_entry_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id",         referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "account_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id",    referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private Account account;
}
