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

    // записываемое поле user_id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // записываемое поле parent_id
    @Column(name = "parent_id")
    private UUID parentId;

    // read-only ассоциация на родителя по (parent_id, user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id",   referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    private Account parent;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private AccountKind kind;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Удобный сеттер: держит в синхроне parent и parentId
    public void setParent(Account parent) {
        this.parent = parent;
        this.parentId = (parent != null ? parent.getId() : null);
        // userId оставляем как есть; БД проверит, что он совпадает с parent.userId
    }
}


enum AccountKind { ASSET, LIABILITY, EQUITY, INCOME, EXPENSE }
