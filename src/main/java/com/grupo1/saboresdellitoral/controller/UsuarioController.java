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

import com.grupo1.saboresdellitoral.dto.ActualizarUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.CambioActivoRequest;
import com.grupo1.saboresdellitoral.dto.CambiarPasswordRequest;
import com.grupo1.saboresdellitoral.dto.CrearUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.UsuarioResponse;
import com.grupo1.saboresdellitoral.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return usuarioService.listar();
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> crear(@RequestBody CrearUsuarioRequest request) {
		UsuarioResponse response = usuarioService.crear(request);
		return ResponseEntity.created(URI.create("/api/usuarios/" + response.id())).body(response);
	}

	@PutMapping("/{id}")
	public UsuarioResponse actualizar(@PathVariable Integer id, @RequestBody ActualizarUsuarioRequest request) {
		return usuarioService.actualizar(id, request);
	}

	@PatchMapping("/{id}/password")
	public UsuarioResponse cambiarPassword(@PathVariable Integer id,
			@RequestBody(required = false) CambiarPasswordRequest request) {
		return usuarioService.cambiarPassword(id, request == null ? null : request.password());
	}

	@PatchMapping("/{id}/activo")
	public UsuarioResponse cambiarActivo(@PathVariable Integer id,
			@RequestBody(required = false) CambioActivoRequest request) {
		return usuarioService.cambiarActivo(id, request == null ? null : request.activo());
	}
}
