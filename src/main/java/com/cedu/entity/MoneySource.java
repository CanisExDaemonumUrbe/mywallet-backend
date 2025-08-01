package com.cedu.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "money_source")
public class MoneySource {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "type", nullable = false, length = Integer.MAX_VALUE)
    private String type;

    @ColumnDefault("'RUB'")
    @Column(name = "currency", nullable = false, length = Integer.MAX_VALUE)
    private String currency;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    public MoneySource() {}

    public MoneySource(UUID userId, String name, String type, String currency, String description) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
