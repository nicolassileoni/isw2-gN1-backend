package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;

public record ProductoRequest(Integer localId, Integer categoriaId, String nombre, String descripcion,
		BigDecimal precio, Integer stock) {
}
