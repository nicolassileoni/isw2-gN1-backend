package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;

public record ProductoCartaResponse(Integer id, String nombre, String descripcion, BigDecimal precio, Integer stock,
		Boolean disponible) {
}
