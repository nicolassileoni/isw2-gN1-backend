package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;

public record ItemSeguimientoResponse(String productoNombre, Integer cantidad, BigDecimal precioUnitario) {
}
