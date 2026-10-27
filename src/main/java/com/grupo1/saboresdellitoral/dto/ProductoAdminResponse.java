package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;

public record ProductoAdminResponse(Integer id, Integer localId, String localNombre, Integer categoriaId,
		String categoriaNombre, String nombre, String descripcion, BigDecimal precio, Integer stock, Boolean activo) {
}
