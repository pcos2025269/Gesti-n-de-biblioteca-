-- Usuarios iniciales (contrasenas con BCrypt).
-- admin@biblioteca.edu         -> Admin123!
-- bibliotecario@biblioteca.edu -> Biblio123!
INSERT INTO usuario (nombre, email, password, estado, rol) VALUES
    ('Administrador', 'admin@biblioteca.edu',
     '$2b$10$uM1D8/1N1YiJMn3ga2lAGuKREzO2yD9tXuRal4EzYE/1OwhHEn9hS', 'ACTIVO', 'ADMIN'),
    ('Bibliotecario', 'bibliotecario@biblioteca.edu',
     '$2b$10$g6KhDw1ECgcVb/OHtVcCbODJS7QZ7DmeOZStcV2jtsgTiB8YNm4ci', 'ACTIVO', 'BIBLIOTECARIO')
ON DUPLICATE KEY UPDATE email = email;
