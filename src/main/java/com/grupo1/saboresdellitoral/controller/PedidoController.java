package com.grupo1.saboresdellitoral.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.CrearPedidoRequest;
import com.grupo1.saboresdellitoral.dto.AvanzarPedidoRequest;
import com.grupo1.saboresdellitoral.dto.PedidoCreadoResponse;
import com.grupo1.saboresdellitoral.dto.PedidoDetalleResponse;
import com.grupo1.saboresdellitoral.service.PedidoService;
import com.grupo1.saboresdellitoral.service.PedidoTransicionService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;
	private final PedidoTransicionService pedidoTransicionService;

	public PedidoController(PedidoService pedidoService, PedidoTransicionService pedidoTransicionService) {
		this.pedidoService = pedidoService;
		this.pedidoTransicionService = pedidoTransicionService;
	}

	@PostMapping
	public ResponseEntity<PedidoCreadoResponse> crear(@RequestBody CrearPedidoRequest request) {
		PedidoCreadoResponse response = pedidoService.crear(request);
		return ResponseEntity.created(URI.create("/api/pedidos/seguimiento/" + response.codigoSeguimiento()))
				.body(response);
	}

	@PatchMapping("/{id}/avanzar")
	public PedidoDetalleResponse avanzar(@PathVariable Integer id,
			@RequestBody(required = false) AvanzarPedidoRequest request) {
		Integer repartidorId = request == null ? null : request.repartidorId();
		return pedidoTransicionService.avanzar(id, repartidorId);
	}
}
