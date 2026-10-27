package com.grupo1.saboresdellitoral.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.ActualizarUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.CrearUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.UsuarioResponse;
import com.grupo1.saboresdellitoral.entity.Rol;
import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.exception.ReglaDeNegocioException;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;

@Service
public class UsuarioService {

	private static final int USERNAME_MAX = 40;
	private static final int NOMBRE_MAX = 100;
	private static final int PASSWORD_MIN = 8;
	private static final int PASSWORD_MAX = 60;

	private final UsuarioRepository usuarioRepository;

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> listar() {
		return usuarioRepository.findAllByOrderByUsernameAsc().stream().map(this::toResponse).toList();
	}

	@Transactional
	public UsuarioResponse crear(CrearUsuarioRequest request) {
		if (request == null) {
			throw new SolicitudInvalidaException("La solicitud es obligatoria");
		}

		String username = normalizarUsername(request.username());
		String password = validarPassword(request.password());
		String nombreCompleto = normalizarNombre(request.nombreCompleto());
		Rol rol = validarRol(request.rol());

		if (usuarioRepository.existsByUsername(username)) {
			throw new ReglaDeNegocioException("Ya existe un usuario con ese nombre de usuario");
		}

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(password);
		usuario.setNombreCompleto(nombreCompleto);
		usuario.setRol(rol);
		usuario.setActivo(true);

		return toResponse(usuarioRepository.saveAndFlush(usuario));
	}

	@Transactional
	public UsuarioResponse actualizar(Integer id, ActualizarUsuarioRequest request) {
		Usuario usuario = buscar(id);
		if (request == null) {
			throw new SolicitudInvalidaException("La solicitud es obligatoria");
		}

		usuario.setNombreCompleto(normalizarNombre(request.nombreCompleto()));
		usuario.setRol(validarRol(request.rol()));

		return toResponse(usuarioRepository.save(usuario));
	}

	@Transactional
	public UsuarioResponse cambiarPassword(Integer id, String password) {
		Usuario usuario = buscar(id);
		usuario.setPassword(validarPassword(password));

		return toResponse(usuarioRepository.save(usuario));
	}

	@Transactional
	public UsuarioResponse cambiarActivo(Integer id, Boolean activo) {
		if (activo == null) {
			throw new SolicitudInvalidaException("El campo activo es obligatorio");
		}

		Usuario usuario = buscar(id);
		usuario.setActivo(activo);

		return toResponse(usuarioRepository.save(usuario));
	}

	private Usuario buscar(Integer id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
	}

	private String normalizarUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new SolicitudInvalidaException("El nombre de usuario es obligatorio");
		}

		String normalizado = username.trim();
		if (normalizado.length() > USERNAME_MAX) {
			throw new SolicitudInvalidaException(
					"El nombre de usuario no puede superar los " + USERNAME_MAX + " caracteres");
		}

		return normalizado;
	}

	private String normalizarNombre(String nombreCompleto) {
		if (nombreCompleto == null || nombreCompleto.isBlank()) {
			throw new SolicitudInvalidaException("El nombre completo es obligatorio");
		}

		String normalizado = nombreCompleto.trim();
		if (normalizado.length() > NOMBRE_MAX) {
			throw new SolicitudInvalidaException(
					"El nombre completo no puede superar los " + NOMBRE_MAX + " caracteres");
		}

		return normalizado;
	}

	private String validarPassword(String password) {
		if (password == null || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
			throw new SolicitudInvalidaException(
					"La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX + " caracteres");
		}

		return password;
	}

	private Rol validarRol(Rol rol) {
		if (rol == null) {
			throw new SolicitudInvalidaException("El rol es obligatorio");
		}

		return rol;
	}

	private UsuarioResponse toResponse(Usuario usuario) {
		return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(),
				usuario.getRol(), usuario.getActivo(), usuario.getFechaAlta());
	}
}
