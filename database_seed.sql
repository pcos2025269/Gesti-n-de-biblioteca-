-- Script SQL para Base de Datos MySQL y Seed Data
CREATE DATABASE IF NOT EXISTS gestor_biblioteca_kinal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestor_biblioteca_kinal;

-- Nota: Las tablas se crean automáticamente si spring.jpa.hibernate.ddl-auto=update está habilitado.
-- Este script inserta los datos semilla obligatorios para el script test-api6.sh.

-- Insertar Usuarios de Prueba (Password para ambos: '123456' en hash BCrypt)
INSERT INTO usuarios (nombre, email, password, rol, estado) VALUES
('Administrador', 'admin@biblioteca.com', '$2a$10$Xptv1h6j8K8Z0e6Z5U0j5e4x0v3k9L3Xv5m9J3k8Z0e6Z5U0j5e4x', 'ADMIN', 'ACTIVO'),
('Lector Prueba', 'lector@biblioteca.com', '$2a$10$Xptv1h6j8K8Z0e6Z5U0j5e4x0v3k9L3Xv5m9J3k8Z0e6Z5U0j5e4x', 'LECTOR', 'ACTIVO')
ON DUPLICATE KEY UPDATE email=email;

-- Insertar Libro Inicial
INSERT INTO libros (titulo, autor, isbn, stock_total, stock_disponible) VALUES
('Spring Boot 4 In Action', 'Autor Java', '978-0134685991', 5, 5)
ON DUPLICATE KEY UPDATE isbn=isbn;
