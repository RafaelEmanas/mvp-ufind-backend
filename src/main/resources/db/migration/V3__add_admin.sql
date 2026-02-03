INSERT INTO users (id, username, email, password, role)
VALUES (
    DEFAULT,
    'Rafael Emanuel',
    'rafael.emanueldv.pro@gmail.com',
    '$2a$10$jdFUQWBfhowAOyaiItXKPugckNvFxaI3R.A7o9omySYTqAJwY6f4e',
    'ROLE_ADMIN'
) ON CONFLICT (email) DO NOTHING;
