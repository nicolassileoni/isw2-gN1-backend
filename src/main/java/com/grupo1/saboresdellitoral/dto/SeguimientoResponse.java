package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.grupo1.saboresdellitoral.entity.EstadoPedido;

public record SeguimientoResponse(String codigoSeguimiento, EstadoPedido estado, String localNombre,
		List<ItemSeguimientoResponse> items, BigDecimal total, LocalDateTime fechaHora) {
}
