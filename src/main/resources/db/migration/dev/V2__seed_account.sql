INSERT INTO account (id, user_id, parent_id, name, kind, is_active)
SELECT id, user_id, parent_id, name, kind, is_active
FROM (
         VALUES
             ('c92a739e-21f6-4b84-b78c-68c8870fd878'::uuid, '${seed_user_id}'::uuid, NULL::uuid, 'Assets',    'ASSET',     TRUE),
             ('7c9b51fc-f3ff-4dc7-a417-1c6b317e2c51'::uuid, '${seed_user_id}'::uuid, NULL::uuid, 'Liability', 'LIABILITY', TRUE),
             ('02f5a412-d9ac-4647-b376-cdc3917c4e9c'::uuid,'${seed_user_id}'::uuid, NULL::uuid, 'Equity',    'EQUITY',    TRUE),
             ('58a6c6e9-332e-4729-bb2b-9341b8a8e5df'::uuid, '${seed_user_id}'::uuid, NULL::uuid, 'Income',    'INCOME',    TRUE),
             ('2a31231c-f37c-4c5f-90c2-bdde1f0d7a55'::uuid, '${seed_user_id}'::uuid, NULL::uuid, 'Expense',   'EXPENSE',   TRUE)
     ) AS v(id, user_id, parent_id, name, kind, is_active)
WHERE NOT EXISTS (
    SELECT 1
    FROM account a
    WHERE a.user_id = v.user_id
      AND a.parent_id IS NOT DISTINCT FROM v.parent_id
      AND a.name = v.name
);
