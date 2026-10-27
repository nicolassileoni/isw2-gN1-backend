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
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.CambioActivoRequest;
import com.grupo1.saboresdellitoral.dto.LocalAdminResponse;
import com.grupo1.saboresdellitoral.dto.LocalRequest;
import com.grupo1.saboresdellitoral.service.LocalService;

@RestController
@RequestMapping("/api/locales")
public class LocalController {

	private final LocalService localService;

	public LocalController(LocalService localService) {
		this.localService = localService;
	}

	@GetMapping("/todos")
	public List<LocalAdminResponse> listarTodos() {
		return localService.listarTodos();
	}

	@PostMapping
	public ResponseEntity<LocalAdminResponse> crear(@RequestBody LocalRequest request) {
		LocalAdminResponse response = localService.crear(request);
		return ResponseEntity.created(URI.create("/api/locales/" + response.id())).body(response);
	}

	@PutMapping("/{id}")
	public LocalAdminResponse actualizar(@PathVariable Integer id, @RequestBody LocalRequest request) {
		return localService.actualizar(id, request);
	}

	@PatchMapping("/{id}/activo")
	public LocalAdminResponse cambiarActivo(@PathVariable Integer id,
			@RequestBody(required = false) CambioActivoRequest request) {
		Boolean activo = request == null ? null : request.activo();
		return localService.cambiarActivo(id, activo);
	}
}
