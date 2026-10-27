package com.grupo1.saboresdellitoral.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.PedidoDetalleResponse;
import com.grupo1.saboresdellitoral.dto.PedidoResumenResponse;
import com.grupo1.saboresdellitoral.dto.SeguimientoResponse;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.service.PedidoConsultaService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoConsultaController {

	private final PedidoConsultaService pedidoConsultaService;

	public PedidoConsultaController(PedidoConsultaService pedidoConsultaService) {
		this.pedidoConsultaService = pedidoConsultaService;
	}

	@GetMapping("/seguimiento/{codigo}")
	public SeguimientoResponse obtenerSeguimiento(@PathVariable String codigo) {
		return pedidoConsultaService.obtenerSeguimiento(codigo);
	}

	@GetMapping
	public List<PedidoResumenResponse> listar(@RequestParam(required = false) EstadoPedido estado) {
		return pedidoConsultaService.listar(estado);
	}

	@GetMapping("/{id}")
	public PedidoDetalleResponse obtenerDetalle(@PathVariable Integer id) {
		return pedidoConsultaService.obtenerDetalle(id);
	}
}
