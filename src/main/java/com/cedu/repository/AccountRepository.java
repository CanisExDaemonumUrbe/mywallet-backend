package com.cedu.repository;

import com.cedu.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID>,
        JpaSpecificationExecutor<Account> {}
