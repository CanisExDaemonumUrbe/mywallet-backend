INSERT INTO account (id, user_id, parent_id, name, kind, is_active)
SELECT *
FROM (
         SELECT CAST('c92a739e-21f6-4b84-b78c-68c8870fd878' AS UUID),
                CAST('${seed_user_id}' AS UUID), NULL, 'Assets',    'ASSET',     TRUE
         UNION ALL
         SELECT CAST('7c9b51fc-f3ff-4dc7-a417-1c6b317e2c51' AS UUID),
                CAST('${seed_user_id}' AS UUID), NULL, 'Liability', 'LIABILITY', TRUE
         UNION ALL
         SELECT CAST('02f5a412-d9ac-4647-b376-cdc3917c4e9c' AS UUID),
                CAST('${seed_user_id}' AS UUID), NULL, 'Equity',    'EQUITY',    TRUE
         UNION ALL
         SELECT CAST('58a6c6e9-332e-4729-bb2b-9341b8a8e5df' AS UUID),
                CAST('${seed_user_id}' AS UUID), NULL, 'Income',    'INCOME',    TRUE
         UNION ALL
         SELECT CAST('2a31231c-f37c-4c5f-90c2-bdde1f0d7a55' AS UUID),
                CAST('${seed_user_id}' AS UUID), NULL, 'Expense',   'EXPENSE',   TRUE
     ) AS v(id, user_id, parent_id, name, kind, is_active)
WHERE NOT EXISTS (
    SELECT 1
    FROM account a
    WHERE a.user_id = v.user_id
      AND ((a.parent_id IS NULL AND v.parent_id IS NULL) OR a.parent_id = v.parent_id)
      AND a.name = v.name
);
