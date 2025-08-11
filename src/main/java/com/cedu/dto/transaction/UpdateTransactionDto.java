package com.cedu.dto.transaction;

import com.cedu.entity.MoneySource;
import com.cedu.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTransactionDto {
    private Instant date;
    private BigDecimal amount;
    private String type;
    private UUID sourceId;
    private String description;
    private Set<UUID> tagsIds;
}
