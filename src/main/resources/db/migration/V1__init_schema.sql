-- V1__init.sql
-- PostgreSQL, schema: public

-- 1) Accounts
create table account
(
    id        uuid primary key,
    user_id   uuid    not null,
    parent_id uuid,
    name      text    not null,
    kind      text    not null check (kind in ('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE')),
    is_active boolean not null default true
);

-- инварианты и производительность
-- уникальность пары (id, user_id) — нужна для составного FK
alter table account
    add constraint uq_account_id_user unique (id, user_id);

-- уникальность имени в пределах (user_id, parent_id)
create unique index uq_account_user_parent_name
    on account (user_id, parent_id, name);

-- составной внешний ключ: (parent_id, user_id) -> (id, user_id)
-- гарантирует, что родитель и ребёнок принадлежат одному user_id
alter table account
    add constraint fk_account_parent_same_user
        foreign key (parent_id, user_id)
            references account (id, user_id)
            on delete restrict;

-- полезные индексы
create index idx_account_user on account (user_id);
create index idx_account_parent_user on account (parent_id, user_id);


-- 2) Journal entries
create table journal_entry
(
    id          uuid primary key,
    user_id     uuid                     not null,
    occurred_at TIMESTAMP WITH TIME ZONE not null,
    booked_at   TIMESTAMP WITH TIME ZONE not null default CURRENT_TIMESTAMP,
    description text,
    reversal_of uuid,
    status      text                     not null default 'POSTED' check (status in ('POSTED', 'VOID'))
);

-- Для составного FK (reversal_of, user_id) -> (id, user_id)
alter table journal_entry
    add constraint uq_je_id_user unique (id, user_id);

-- Разрешаем реверс ТОЛЬКО внутри того же пользователя
alter table journal_entry
    add constraint fk_je_reversal_same_user
        foreign key (reversal_of, user_id)
            references journal_entry (id, user_id)
            on delete restrict;

-- Один реверс на исходную проводку
create unique index uq_je_reversal_once
    on journal_entry (reversal_of);

-- (Опционально) запрет «будущего бронирования», если нужно
alter table journal_entry
    add constraint ck_je_occurred_le_booked check (occurred_at <= booked_at);

-- Индексы
create index idx_je_user on journal_entry (user_id);
create index idx_je_occurred on journal_entry (occurred_at);
create index idx_je_reversal_user on journal_entry (reversal_of, user_id);

-- 3) Postings
create table posting
(
    id               uuid primary key,
    user_id          uuid           not null,
    journal_entry_id uuid           not null,
    account_id       uuid           not null,
    side             text           not null check (side in ('DEBIT', 'CREDIT')),
    amount           numeric(18, 2) not null check (amount > 0),

    -- для составных внешних ключей
    constraint uq_posting_id_user unique (id, user_id),

    -- изоляция арендатора: ссылка только на JE того же user_id
    constraint fk_posting_je
        foreign key (journal_entry_id, user_id)
            references journal_entry (id, user_id)
            on delete cascade,

    -- изоляция арендатора: ссылка только на Account того же user_id
    constraint fk_posting_acc
        foreign key (account_id, user_id)
            references account (id, user_id)
);

-- индексы под частые запросы и под FK-проверки
create index idx_posting_user on posting (user_id);
create index idx_posting_je_user on posting (journal_entry_id, user_id);
create index idx_posting_acc_user on posting (account_id, user_id);
