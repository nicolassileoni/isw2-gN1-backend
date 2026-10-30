-- =====================================================================
-- Sabores del Litoral: datos de demostración
-- Se cargan además de data.sql para poder probar la app sin crear todo
-- desde cero. Solo aplica al entorno local; los tests usan data.sql.
-- =====================================================================

-- Usuario operador de prueba
INSERT INTO usuario (id, username, password_hash, nombre_completo, rol, activo)
VALUES (2, 'operador', '$2a$10$gNJgyylRCAZE/ZcOSjyDy.WMwvdS3wBFWEkmErracd.ZpQetd/U0y', 'Operador de Prueba', 'OPERADOR', TRUE);
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 3;

-- Locales: dos activos y uno inactivo
INSERT INTO local (id, nombre, direccion, telefono, activo)
VALUES (1, 'Sabores del Puerto', 'Belgrano 1250', '3425000001', TRUE);
INSERT INTO local (id, nombre, direccion, telefono, activo)
VALUES (2, 'La Cocina de la Costa', 'San Martín 850', '3425000002', TRUE);
INSERT INTO local (id, nombre, direccion, telefono, activo)
VALUES (3, 'Rotisería El Litoral', 'Rivadavia 320', '3425000003', FALSE);
ALTER TABLE local ALTER COLUMN id RESTART WITH 4;

-- Productos del local 1
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (1, 1, 1, 'Pizza muzzarella', 'Salsa de tomate y muzzarella', 9500.00, 12, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (2, 1, 1, 'Pizza fugazzeta', 'Cebolla, muzzarella y orégano', 10500.00, 4, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (3, 1, 2, 'Hamburguesa clásica', 'Carne, queso, lechuga y tomate', 8500.00, 8, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (4, 1, 3, 'Empanada de carne', 'Carne cortada a cuchillo', 1200.00, 30, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (5, 1, 4, 'Gaseosa cola 1,5 L', 'Bebida sin alcohol', 2800.00, 0, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (6, 1, 5, 'Flan casero', 'Flan con caramelo', 3500.00, 6, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (7, 1, 1, 'Pizza fuera de carta', 'Producto inactivo para probar el catálogo', 8000.00, 5, FALSE);

-- Productos del local 2
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (8, 2, 1, 'Pizza napolitana', 'Tomate, muzzarella y ajo', 9800.00, 10, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (9, 2, 2, 'Hamburguesa doble', 'Dos medallones y cheddar', 11500.00, 5, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (10, 2, 4, 'Agua mineral 500 ml', 'Agua sin gas', 1500.00, 20, TRUE);
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (11, 2, 5, 'Helado artesanal', 'Dos bochas a elección', 4200.00, 10, TRUE);

-- Producto del local inactivo
INSERT INTO producto (id, local_id, categoria_id, nombre, descripcion, precio, stock, activo)
VALUES (12, 3, 1, 'Pizza especial', 'Producto de un local inactivo', 12000.00, 5, TRUE);
ALTER TABLE producto ALTER COLUMN id RESTART WITH 13;

-- Clientes de demostración
INSERT INTO cliente (id, nombre, telefono) VALUES (1, 'Lucía Gómez', '3425000100');
INSERT INTO cliente (id, nombre, telefono) VALUES (2, 'Martín Paz', '3425000200');
INSERT INTO cliente (id, nombre, telefono) VALUES (3, 'Sofía Ríos', '3425000300');
ALTER TABLE cliente ALTER COLUMN id RESTART WITH 4;

-- Pedidos en distintos estados para probar seguimiento y despacho
INSERT INTO pedido (id, codigo_seguimiento, cliente_id, local_id, repartidor_id,
                    direccion_entrega, estado, subtotal, descuento, costo_envio, total)
VALUES (1, 'DEMO0001', 1, 1, 2, 'Urquiza 45', 'ENTREGADO', 21000.00, 2100.00, 1500.00, 20400.00);

INSERT INTO pedido (id, codigo_seguimiento, cliente_id, local_id, repartidor_id,
                    direccion_entrega, estado, subtotal, descuento, costo_envio, total)
VALUES (2, 'DEMO0002', 2, 2, 1, 'Mitre 720', 'EN_CAMINO', 13000.00, 0.00, 1500.00, 14500.00);

INSERT INTO pedido (id, codigo_seguimiento, cliente_id, local_id, repartidor_id,
                    direccion_entrega, estado, subtotal, descuento, costo_envio, total)
VALUES (3, 'DEMO0003', 3, 1, NULL, 'Sarmiento 110', 'RECIBIDO', 3600.00, 0.00, 1500.00, 5100.00);

INSERT INTO pedido (id, codigo_seguimiento, cliente_id, local_id, repartidor_id,
                    direccion_entrega, estado, subtotal, descuento, costo_envio, total)
VALUES (4, 'DEMO0004', 2, 1, NULL, 'San Lorenzo 330', 'EN_PREPARACION', 13000.00, 0.00, 1500.00, 14500.00);

INSERT INTO item_pedido (id, pedido_id, producto_id, cantidad, precio_unitario)
VALUES (1, 1, 2, 2, 10500.00);
INSERT INTO item_pedido (id, pedido_id, producto_id, cantidad, precio_unitario)
VALUES (2, 2, 9, 1, 11500.00);
INSERT INTO item_pedido (id, pedido_id, producto_id, cantidad, precio_unitario)
VALUES (3, 2, 10, 1, 1500.00);
INSERT INTO item_pedido (id, pedido_id, producto_id, cantidad, precio_unitario)
VALUES (4, 3, 4, 3, 1200.00);
INSERT INTO item_pedido (id, pedido_id, producto_id, cantidad, precio_unitario)
VALUES (5, 4, 1, 1, 9500.00);
INSERT INTO item_pedido (id, pedido_id, producto_id, cantidad, precio_unitario)
VALUES (6, 4, 6, 1, 3500.00);

ALTER TABLE pedido ALTER COLUMN id RESTART WITH 5;
ALTER TABLE item_pedido ALTER COLUMN id RESTART WITH 7;
