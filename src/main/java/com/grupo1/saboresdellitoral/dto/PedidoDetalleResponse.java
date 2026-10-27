package com.grupo1.saboresdellitoral.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.grupo1.saboresdellitoral.entity.EstadoPedido;

public record PedidoDetalleResponse(Integer id, String codigoSeguimiento, EstadoPedido estado,
		LocalResumenResponse local, ClienteResponse cliente, String direccionEntrega,
		RepartidorResumenResponse repartidor, LocalDateTime fechaHora, List<ItemDetalleResponse> items,
		BigDecimal subtotal, BigDecimal descuento, BigDecimal costoEnvio, BigDecimal total) {
}
