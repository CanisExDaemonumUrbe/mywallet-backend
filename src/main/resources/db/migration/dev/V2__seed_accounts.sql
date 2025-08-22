-- CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- если используешь gen_random_uuid()
INSERT INTO account (id, user_id, parent_id, name, kind, is_active)
VALUES
    (gen_random_uuid(), '${seed_user_id}'::uuid, NULL, 'Assets',    'ASSET',     true),
    (gen_random_uuid(), '${seed_user_id}'::uuid, NULL, 'Liability', 'LIABILITY', true),
    (gen_random_uuid(), '${seed_user_id}'::uuid, NULL, 'Equity',    'EQUITY',    true),
    (gen_random_uuid(), '${seed_user_id}'::uuid, NULL, 'Income',    'INCOME',    true),
    (gen_random_uuid(), '${seed_user_id}'::uuid, NULL, 'Expense',   'EXPENSE',   true)
ON CONFLICT (user_id, parent_id, name) DO NOTHING;
