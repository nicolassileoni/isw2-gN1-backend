package com.grupo1.saboresdellitoral.dto;

import java.util.List;

public record RepartidorEnCalleResponse(Integer repartidorId, String nombreCompleto, Integer cantidadPedidos,
		List<String> codigosPedidos) {
}
