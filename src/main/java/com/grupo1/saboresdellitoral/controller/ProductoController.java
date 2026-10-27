package com.grupo1.saboresdellitoral.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.CambioActivoRequest;
import com.grupo1.saboresdellitoral.dto.ProductoAdminResponse;
import com.grupo1.saboresdellitoral.dto.ProductoRequest;
import com.grupo1.saboresdellitoral.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private final ProductoService productoService;

	public ProductoController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@GetMapping
	public List<ProductoAdminResponse> listar(@RequestParam(required = false) Integer localId) {
		return productoService.listar(localId);
	}

	@PostMapping
	public ResponseEntity<ProductoAdminResponse> crear(@RequestBody ProductoRequest request) {
		ProductoAdminResponse response = productoService.crear(request);
		return ResponseEntity.created(URI.create("/api/productos/" + response.id())).body(response);
	}

	@PutMapping("/{id}")
	public ProductoAdminResponse actualizar(@PathVariable Integer id, @RequestBody ProductoRequest request) {
		return productoService.actualizar(id, request);
	}

	@PatchMapping("/{id}/activo")
	public ProductoAdminResponse cambiarActivo(@PathVariable Integer id,
			@RequestBody(required = false) CambioActivoRequest request) {
		Boolean activo = request == null ? null : request.activo();
		return productoService.cambiarActivo(id, activo);
	}
}
