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
