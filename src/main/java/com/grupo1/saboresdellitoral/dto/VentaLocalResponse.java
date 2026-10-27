package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;

public record VentaLocalResponse(Integer localId, String localNombre, long cantidadPedidos, BigDecimal montoVendido) {
}
