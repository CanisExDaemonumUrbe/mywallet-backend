-- V1__init.sql
-- PostgreSQL, schema: public

-- 1) Accounts
create table account (
                         id uuid primary key,
                         user_id uuid not null,
                         parent_id uuid references account(id),
                         name text not null,
                         kind text not null check (kind in ('asset','liability','equity','income','expense')),
                         currency text,
                         is_active boolean not null default true
);

create index idx_account_user   on account(user_id);
create index idx_account_parent on account(parent_id);
create unique index uq_account_user_parent_name on account(user_id, parent_id, name);

-- 2) Journal entries
create table journal_entry (
                               id uuid primary key,
                               user_id uuid not null,
                               occurred_at timestamp not null,
                               booked_at   timestamp not null default now(),
                               description  text,
                               external_ref text,
                               reversal_of uuid references journal_entry(id),
                               status text not null default 'posted' check (status in ('posted','void'))
);

create index idx_je_user      on journal_entry(user_id);
create index idx_je_occurred  on journal_entry(occurred_at);
create index idx_je_reversal  on journal_entry(reversal_of);

-- 3) Postings
create table posting (
                         id uuid primary key,
                         journal_entry_id uuid not null references journal_entry(id) on delete cascade,
                         account_id       uuid not null references account(id),
                         side   text not null check (side in ('debit','credit')),
                         amount numeric(18,2) not null check (amount > 0)
);

create index idx_posting_je  on posting(journal_entry_id);
create index idx_posting_acc on posting(account_id);

-----------------------------------------------------------------------------


-- Источник средств
create table money_source (
                              id uuid primary key,
                              user_id uuid not null,
                              name text not null,
                              type text not null,
                              currency text not null default 'RUB',
                              description text
);

-- Транзакции
create table transaction (
                             id uuid primary key,
                             user_id uuid not null,
                             date timestamp not null,
                             amount numeric(12,2) not null,
                             type text not null check (type in ('income', 'expense')),
                             source_id uuid not null references money_source(id),
                             description text
);

-- Теги пользователя
create table tag (
                     id uuid primary key,
                     user_id uuid not null,
                     name text not null,
                     unique (user_id, name)
);

-- Связь "многие ко многим" между транзакциями и тегами
create table transaction_tag (
                                 transaction_id uuid not null references transaction(id) on delete cascade,
                                 tag_id uuid not null references tag(id) on delete cascade,
                                 primary key (transaction_id, tag_id)
);
