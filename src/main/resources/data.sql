-- admin@biblioteca.com         -> Admin123*
-- bibliotecario@biblioteca.com -> Biblio123*
INSERT INTO usuario (nombre, email, password, estado, rol) VALUES
    ('Administrador', 'admin@biblioteca.com',
     '$2b$10$5lm0XR6TG41Md0lOjFpLrOMcv2YvtT4U8Stk65TCFKqm7ILYVLXq2', 'ACTIVO', 'ADMIN'),
    ('Bibliotecario', 'bibliotecario@biblioteca.com',
     '$2b$10$IPOVT8sNZ9T1YL21vCnvMuh.OPJeRC5.rgcoP4H4LYlKpX118TC/O', 'ACTIVO', 'BIBLIOTECARIO')
ON DUPLICATE KEY UPDATE email = email;
