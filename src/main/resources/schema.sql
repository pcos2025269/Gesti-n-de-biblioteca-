CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    rol VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO', 'SANCIONADO')),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN', 'BIBLIOTECARIO', 'LECTOR'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS libro (
    id BIGINT NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    stock_total INT NOT NULL,
    stock_disponible INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_libro_isbn UNIQUE (isbn),
    CONSTRAINT ck_libro_stock CHECK (stock_disponible >= 0 AND stock_disponible <= stock_total),
    KEY idx_libro_titulo (titulo),
    KEY idx_libro_categoria (categoria)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS prestamo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    libro_id BIGINT NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE NULL,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libro (id),
    CONSTRAINT ck_prestamo_estado CHECK (estado IN ('ACTIVO', 'DEVUELTO', 'ATRASADO')),
    KEY idx_prestamo_usuario_estado (usuario_id, estado),
    KEY idx_prestamo_estado_fecha_esperada (estado, fecha_devolucion_esperada)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
