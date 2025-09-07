
-- ========================================
-- Base de datos: oficios_db
-- ========================================

CREATE DATABASE IF NOT EXISTS oficios_db;
USE oficios_db;

-- ========================================
-- Tabla de usuarios
-- ========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- ========================================
-- Tabla de oficios
-- ========================================
CREATE TABLE IF NOT EXISTS oficios (
    id_oficio INT AUTO_INCREMENT PRIMARY KEY,
    numero_oficio INT NOT NULL UNIQUE,
    persona_dirigida VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    asunto TEXT NOT NULL,
    fecha DATE NOT NULL,
    hash_firma CHAR(32) GENERATED ALWAYS AS 
    (MD5(CONCAT(numero_oficio, persona_dirigida, area, asunto, fecha))) STORED
);

-- ========================================
-- Trigger para asegurar consecutivo en numero_oficio
-- ========================================
DELIMITER //
CREATE TRIGGER before_insert_oficio
BEFORE INSERT ON oficios
FOR EACH ROW
BEGIN
    DECLARE next_num INT;
    -- Obtener el último numero_oficio
    SELECT IFNULL(MAX(numero_oficio), 0) + 1 INTO next_num FROM oficios;
    SET NEW.numero_oficio = next_num;
END //
DELIMITER ;
