-- =====================================================================
-- Sabores del Litoral: datos iniciales (data.sql)
-- Solo las tablas que NO tienen CRUD en el sistema, más el administrador inicial.
-- Locales, productos, clientes, pedidos e ítems arrancan vacíos.
-- =====================================================================

-- Administrador inicial: usuario admin, contraseña Admin2026! (guardada con BCrypt)
INSERT INTO usuario (id, username, password_hash, nombre_completo, rol, activo)
VALUES (1, 'admin', '$2a$10$nlQuA4lskuCHKf20.SP0Y.qxRBSzebNuT5bVGy43ySrDtDMz9ryQ2', 'Administrador de Sabores', 'ADMIN', TRUE);

-- Categorías de la carta (comunes a todos los locales)
INSERT INTO categoria (id, nombre) VALUES (1, 'Pizzas');
INSERT INTO categoria (id, nombre) VALUES (2, 'Hamburguesas');
INSERT INTO categoria (id, nombre) VALUES (3, 'Empanadas');
INSERT INTO categoria (id, nombre) VALUES (4, 'Bebidas');
INSERT INTO categoria (id, nombre) VALUES (5, 'Postres');

-- Repartidores
INSERT INTO repartidor (id, nombre_completo, telefono) VALUES (1, 'Carlos Benítez', '3425100200');
INSERT INTO repartidor (id, nombre_completo, telefono) VALUES (2, 'Micaela Ortiz', '3425100300');
INSERT INTO repartidor (id, nombre_completo, telefono) VALUES (3, 'Iván Paredes', '3425100400');

-- Los ids anteriores se cargaron a mano: se adelantan los contadores
ALTER TABLE usuario    ALTER COLUMN id RESTART WITH 2;
ALTER TABLE categoria  ALTER COLUMN id RESTART WITH 6;
ALTER TABLE repartidor ALTER COLUMN id RESTART WITH 4;
