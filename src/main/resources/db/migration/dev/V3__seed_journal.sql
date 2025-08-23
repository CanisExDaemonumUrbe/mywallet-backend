INSERT INTO journal_entry (id, user_id, occurred_at, booked_at, description, reversal_of, status)
SELECT id, user_id, occurred_at, booked_at, description, reversal_of, status
FROM (
         VALUES
             -- 1
             ('11111111-aaaa-4bbb-8ccc-111111111111'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days',
              'Initial balance load', NULL::uuid, 'POSTED'),

             -- 2
             ('22222222-aaaa-4bbb-8ccc-222222222222'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days',
              'Purchase: Office supplies', NULL::uuid, 'POSTED'),

             -- 3
             ('33333333-aaaa-4bbb-8ccc-333333333333'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days',
              'Payment: Rent', NULL::uuid, 'POSTED'),

             -- 4
             ('44444444-aaaa-4bbb-8ccc-444444444444'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days',
              'Revenue: Product A', NULL::uuid, 'POSTED'),

             -- 5
             ('55555555-aaaa-4bbb-8ccc-555555555555'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days',
              'Revenue: Product B', NULL::uuid, 'POSTED'),

             -- 6
             ('66666666-aaaa-4bbb-8ccc-666666666666'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days',
              'Expense: Marketing', NULL::uuid, 'POSTED'),

             -- 7
             ('77777777-aaaa-4bbb-8ccc-777777777777'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days',
              'Expense: Salaries', NULL::uuid, 'POSTED'),

             -- 8 (VOID)
             ('88888888-aaaa-4bbb-8ccc-888888888888'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days',
              'Cancelled transaction', NULL::uuid, 'VOID'),

             -- 9 (реверс на запись #3 "Payment: Rent")
             ('99999999-aaaa-4bbb-8ccc-999999999999'::uuid, '${seed_user_id}'::uuid,
              NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day',
              'Reversal of rent payment', '33333333-aaaa-4bbb-8ccc-333333333333'::uuid, 'POSTED'),

             -- 10
             ('aaaaaaaa-aaaa-4bbb-8ccc-aaaaaaaaaaaa'::uuid, '${seed_user_id}'::uuid,
              NOW(), NOW(),
              'Revenue: Product C', NULL::uuid, 'POSTED')
     ) AS v(id, user_id, occurred_at, booked_at, description, reversal_of, status)
WHERE NOT EXISTS (
    SELECT 1
    FROM journal_entry je
    WHERE je.user_id = v.user_id
      AND je.id     = v.id
);
