package com.grupo1.saboresdellitoral.dto;

import java.util.List;

public record CrearPedidoRequest(Integer localId, String direccionEntrega, ClientePedidoRequest cliente,
		List<ItemPedidoRequest> items) {
}
