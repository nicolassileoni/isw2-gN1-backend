package com.grupo1.saboresdellitoral.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.CartaResponse;
import com.grupo1.saboresdellitoral.dto.CategoriaResponse;
import com.grupo1.saboresdellitoral.dto.LocalResponse;
import com.grupo1.saboresdellitoral.dto.RepartidorResponse;
import com.grupo1.saboresdellitoral.service.CatalogoService;

@RestController
@RequestMapping("/api")
public class CatalogoController {

	private final CatalogoService catalogoService;

	public CatalogoController(CatalogoService catalogoService) {
		this.catalogoService = catalogoService;
	}

	@GetMapping("/categorias")
	public ResponseEntity<List<CategoriaResponse>> listarCategorias() {
		return ResponseEntity.ok(catalogoService.listarCategorias());
	}

	@GetMapping("/locales")
	public ResponseEntity<List<LocalResponse>> listarLocales() {
		return ResponseEntity.ok(catalogoService.listarLocales());
	}

	@GetMapping("/locales/{id}/carta")
	public ResponseEntity<CartaResponse> obtenerCarta(@PathVariable Integer id) {
		return ResponseEntity.ok(catalogoService.obtenerCarta(id));
	}

	@GetMapping("/repartidores")
	public ResponseEntity<List<RepartidorResponse>> listarRepartidores() {
		return ResponseEntity.ok(catalogoService.listarRepartidores());
	}
}
