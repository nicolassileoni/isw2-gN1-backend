package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;

public record ItemDetalleResponse(Integer productoId, String productoNombre, Integer cantidad,
		BigDecimal precioUnitario, BigDecimal subtotal) {
}
