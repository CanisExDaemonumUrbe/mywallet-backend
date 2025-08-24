package com.cedu.repository;

import com.cedu.dto.account.FilterAccountDto;
import com.cedu.entity.Account;
import com.cedu.enums.AccountKind;
import com.cedu.specification.AccountSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    private final UUID USER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID USER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private Account rootUser1;     // parent=null
    private Account cashUser1;     // parent=rootUser1
    private Account incomeUser1;   // parent=null
    private Account liabilityUser2;// parent=null, inactive

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        // root (user1)
        rootUser1 = new Account();
        rootUser1.setUserId(USER_1);
        rootUser1.setName("Root Assets");
        rootUser1.setKind(AccountKind.ASSET);
        rootUser1.setIsActive(true);
        rootUser1 = repository.save(rootUser1);

        // child (user1) — важно: userId тот же, затем setParent
        cashUser1 = new Account();
        cashUser1.setUserId(USER_1);
        cashUser1.setName("Cash");
        cashUser1.setKind(AccountKind.ASSET);
        cashUser1.setIsActive(true);
        cashUser1.setParent(rootUser1); // синхронизирует parentId
        cashUser1 = repository.save(cashUser1);

        // income (user1)
        incomeUser1 = new Account();
        incomeUser1.setUserId(USER_1);
        incomeUser1.setName("Salary");
        incomeUser1.setKind(AccountKind.INCOME);
        incomeUser1.setIsActive(true);
        incomeUser1 = repository.save(incomeUser1);

        // user2 liability, inactive
        liabilityUser2 = new Account();
        liabilityUser2.setUserId(USER_2);
        liabilityUser2.setName("Loans");
        liabilityUser2.setKind(AccountKind.LIABILITY);
        liabilityUser2.setIsActive(false);
        liabilityUser2 = repository.save(liabilityUser2);
    }

    @Test
    void findAll_noFilters_returnsAll() {
        var filter = FilterAccountDto.builder().build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));
        assertThat(result).hasSize(4);
    }

    @Test
    void findByUserId_onlyUser1() {
        var filter = FilterAccountDto.builder().userId(USER_1).build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(a -> USER_1.equals(a.getUserId()));
    }

    @Test
    void findByParentId_childrenOnly() {
        var filter = FilterAccountDto.builder().parentId(rootUser1.getId()).build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(cashUser1.getId());
    }

    @Test
    void findByName_like() {
        var filter = FilterAccountDto.builder().name("Cash").build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Cash");
    }

    @Test
    void findByKind_assetOnly() {
        var filter = FilterAccountDto.builder().kind(AccountKind.ASSET).build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        // rootUser1 + cashUser1
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(rootUser1.getId(), cashUser1.getId());
    }

    @Test
    void findByIsActive_trueOnly() {
        var filter = FilterAccountDto.builder().isActive(true).build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(rootUser1.getId(), cashUser1.getId(), incomeUser1.getId());
    }

    @Test
    void findByIsActive_falseOnly() {
        var filter = FilterAccountDto.builder().isActive(false).build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(liabilityUser2.getId());
    }

    @Test
    void combined_user1_kindAsset_active() {
        var filter = FilterAccountDto.builder()
                .userId(USER_1)
                .kind(AccountKind.ASSET)
                .isActive(true)
                .build();

        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(rootUser1.getId(), cashUser1.getId());
    }

    @Test
    void byId_exact() {
        var filter = FilterAccountDto.builder().id(incomeUser1.getId()).build();
        var result = repository.findAll(AccountSpecification.withFilters(filter));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Salary");
    }

    @Test
    void noMatch_returnsEmpty() {
        var filter = FilterAccountDto.builder()
                .userId(USER_1)
                .kind(AccountKind.LIABILITY)
                .build();

        var result = repository.findAll(AccountSpecification.withFilters(filter));
        assertThat(result).isEmpty();
    }
}
