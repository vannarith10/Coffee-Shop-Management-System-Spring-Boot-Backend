

-- 1. Set ALL users to FULL_DAY
UPDATE users SET shift_type = 'FULL_DAY';

-- 2. Clear ALL existing schedules (clean slate)
DELETE FROM user_schedules;

-- 3. Assign different 5-day combinations based on user creation order
WITH numbered_users AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) as n
    FROM users
),
     day_patterns AS (
         SELECT * FROM (VALUES
                            (1, 'MONDAY'),    (1, 'TUESDAY'),  (1, 'WEDNESDAY'), (1, 'THURSDAY'), (1, 'FRIDAY'),
                            (2, 'MONDAY'),    (2, 'TUESDAY'),  (2, 'WEDNESDAY'), (2, 'THURSDAY'), (2, 'SATURDAY'),
                            (3, 'MONDAY'),    (3, 'TUESDAY'),  (3, 'WEDNESDAY'), (3, 'FRIDAY'),   (3, 'SUNDAY'),
                            (4, 'TUESDAY'),   (4, 'WEDNESDAY'),(4, 'THURSDAY'),  (4, 'FRIDAY'),   (4, 'SATURDAY')
                       ) AS t(pattern_num, day_name)
     )
INSERT INTO user_schedules (user_id, schedule_day)
SELECT u.id, d.day_name
FROM numbered_users u
         JOIN day_patterns d ON u.n = d.pattern_num;