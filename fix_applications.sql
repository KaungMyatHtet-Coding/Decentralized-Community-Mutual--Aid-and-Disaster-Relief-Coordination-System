INSERT INTO volunteer_applications (application_note, applied_at, operating_township, proof_image_url, status, user_id)
SELECT 'System Generated Mock Data', NOW(), township, 'https://example.com/mock.jpg', 'APPROVED', id 
FROM users 
WHERE role = 'ROLE_VOLUNTEER' 
AND id NOT IN (SELECT user_id FROM volunteer_applications);
