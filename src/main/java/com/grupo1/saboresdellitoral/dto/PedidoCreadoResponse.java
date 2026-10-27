package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grupo1.saboresdellitoral.entity.EstadoPedido;

public record PedidoCreadoResponse(String codigoSeguimiento, BigDecimal total, EstadoPedido estado,
		LocalDateTime fechaHora) {
}
