package com.grupo1.saboresdellitoral.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.VentaLocalResponse;
import com.grupo1.saboresdellitoral.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

	private final VentaService ventaService;

	public VentaController(VentaService ventaService) {
		this.ventaService = ventaService;
	}

	@GetMapping
	public List<VentaLocalResponse> listarPorLocal(@RequestParam(required = false) String desde,
			@RequestParam(required = false) String hasta) {
		return ventaService.listarPorLocal(desde, hasta);
	}
}
