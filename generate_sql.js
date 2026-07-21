const fs = require('fs');

const townships = [
  'Ahlon', 'Bahan', 'Botataung', 'Dagon', 'Dagon Seikkan', 'Dawbon', 'East Dagon', 'Hlaing', 'Hlaingthaya', 'Insein', 'Kamayut', 'Kyauktada', 'Kyimyindaing', 'Lanmadaw', 'Latha', 'Mayangon', 'Mingaladon', 'Mingala Taungnyunt', 'North Dagon', 'North Okkalapa', 'Pabedan', 'Pazundaung', 'Sanchaung', 'Seikkan', 'Shwepyitha', 'South Dagon', 'South Okkalapa', 'Tamwe', 'Thaketa', 'Thingangyun', 'Yankin'
];

let sql = `-- Insert 124 Volunteers (4 per township)
INSERT INTO users (username, email, password, role, is_verified, full_name, township, profile_completed, created_at, updated_at, has_vehicle, years_of_experience, is_active)
VALUES
`;

const values = [];

for (const t of townships) {
  for (let i = 1; i <= 4; i++) {
    const tSlug = t.toLowerCase().replace(/ /g, '_');
    const username = `vol_${tSlug}_${i}`;
    const email = `${username}@hnaungkyoe.com`;
    // password123 hash
    const pass = '$2a$10$6ZxRWzLjuh5AcWwv6YAM/uGS4duWTT.4TWNlcchwzIPm54lALgHY6';
    const fullName = `${t} Volunteer ${i}`;
    
    values.push(`('${username}', '${email}', '${pass}', 'ROLE_VOLUNTEER', true, '${fullName}', '${t}', true, NOW(), NOW(), ${i%2===0}, ${i}, true)`);
  }
}

sql += values.join(',\n') + ';\n';

fs.writeFileSync('insert_volunteers.sql', sql);
console.log('insert_volunteers.sql created');

const superAdminSql = `-- Reset Super Admin
INSERT INTO users (username, email, password, role, is_verified, full_name, township, profile_completed, created_at, updated_at, has_vehicle, years_of_experience, is_active)
SELECT 'superadmin', 'superadmin@hnaungkyoe.com', '$2a$10$6ZxRWzLjuh5AcWwv6YAM/uGS4duWTT.4TWNlcchwzIPm54lALgHY6', 'ROLE_SUPER_ADMIN', true, 'Super Admin', 'Yangon', true, NOW(), NOW(), false, 5, true
WHERE NOT EXISTS (SELECT id FROM users WHERE username = 'superadmin');
`;

fs.writeFileSync('reset_superadmin.sql', superAdminSql);
console.log('reset_superadmin.sql created');
