package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grupo1.saboresdellitoral.entity.EstadoPedido;

public record PedidoResumenResponse(Integer id, String codigoSeguimiento, EstadoPedido estado, String localNombre,
		String clienteNombre, LocalDateTime fechaHora, BigDecimal total) {
}
