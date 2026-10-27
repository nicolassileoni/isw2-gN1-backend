package com.grupo1.saboresdellitoral.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.CrearPedidoRequest;
import com.grupo1.saboresdellitoral.dto.PedidoCreadoResponse;
import com.grupo1.saboresdellitoral.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@PostMapping
	public ResponseEntity<PedidoCreadoResponse> crear(@RequestBody CrearPedidoRequest request) {
		PedidoCreadoResponse response = pedidoService.crear(request);
		return ResponseEntity.created(URI.create("/api/pedidos/seguimiento/" + response.codigoSeguimiento()))
				.body(response);
	}
}
