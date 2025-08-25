package com.cedu.service;

import com.cedu.exception.InvalidUserException;
import com.cedu.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class OwnershipValidator {

    public <ID, T> T findAndValidate(
            ID id,
            Function<ID, Optional<T>> finder,
            Function<T, UUID> ownerExtractor,
            UUID requestUserId,
            String entityName
    ) {
        if (id == null) return null;

        T entity = finder.apply(id)
                .orElseThrow(() -> new NotFoundException(entityName + " with id " + id + " not found"));

        UUID ownerId = ownerExtractor.apply(entity);
        if (!Objects.equals(ownerId, requestUserId)) {
            throw new InvalidUserException(
                    entityName + " user_id mismatch: request:= " + requestUserId + ", expected:= " + ownerId
            );
        }
        return entity;
    }
}

