package com.cedu.entity;

import com.cedu.enums.JournalEntryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "journal_entry")
@Getter @Setter
public class JournalEntry {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "booked_at", nullable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "description")
    private String description;

    @Column(name = "reversal_of")
    private UUID reversalOfId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "reversal_of", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id",     referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private JournalEntry reversalOf;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JournalEntryStatus status = JournalEntryStatus.POSTED;

    @PrePersist
    void prePersist() {
        if (bookedAt == null) bookedAt = OffsetDateTime.now();
    }

    public void setReversalOf(JournalEntry origin) {
        this.reversalOf = origin;
        this.reversalOfId = (origin != null ? origin.getId() : null);
    }
}
