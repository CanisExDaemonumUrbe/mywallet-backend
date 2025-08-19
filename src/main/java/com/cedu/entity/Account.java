package com.cedu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "account")
@Getter @Setter
public class Account {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "parent_id", referencedColumnName = "id"),
            @JoinColumn(name = "user_id",   referencedColumnName = "user_id",
                    insertable = false, updatable = false)
    })
    private Account parent;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private AccountKind kind;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

enum AccountKind { ASSET, LIABILITY, EQUITY, INCOME, EXPENSE }
