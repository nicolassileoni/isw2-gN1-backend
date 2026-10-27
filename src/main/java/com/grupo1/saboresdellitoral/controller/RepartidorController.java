package com.grupo1.saboresdellitoral.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.RepartidorEnCalleResponse;
import com.grupo1.saboresdellitoral.service.RepartidorService;

@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

	private final RepartidorService repartidorService;

	public RepartidorController(RepartidorService repartidorService) {
		this.repartidorService = repartidorService;
	}

	@GetMapping("/en-calle")
	public List<RepartidorEnCalleResponse> listarEnCalle() {
		return repartidorService.listarEnCalle();
	}
}
