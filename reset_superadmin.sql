-- Reset Super Admin
INSERT INTO users (username, email, password, role, is_verified, full_name, township, profile_completed, created_at, updated_at, has_vehicle, years_of_experience, is_active)
SELECT 'superadmin', 'superadmin@hnaungkyoe.com', '$2a$10$6ZxRWzLjuh5AcWwv6YAM/uGS4duWTT.4TWNlcchwzIPm54lALgHY6', 'ROLE_SUPER_ADMIN', true, 'Super Admin', 'Yangon', true, NOW(), NOW(), false, 5, true
WHERE NOT EXISTS (SELECT id FROM users WHERE username = 'superadmin');
