package com.cedu.dto.account;

import com.cedu.enums.AccountKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestAccountDto {
    @NotNull(message = "user_id is required")
    private UUID userId;

    private UUID parentId;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "kind is required")
    private AccountKind kind;

    private Boolean isActive;
}
