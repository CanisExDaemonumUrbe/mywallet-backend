create table money_source (
                              id uuid primary key,
                              user_id uuid not null,
                              name text not null,
                              type text not null,
                              currency text not null default 'RUB',
                              description text
);

create table transaction (
                             id uuid primary key,
                             user_id uuid not null,
                             date timestamp not null,
                             amount numeric(12,2) not null,
                             type text not null check ( type in ('income', 'expense') ),
                             source_id uuid not null references money_source(id),
                             description text
);

create table transaction_tag (
                                 transaction_id uuid not null references transaction(id) on delete cascade,
                                 tag text not null,
                                 primary key (transaction_id, tag)
);