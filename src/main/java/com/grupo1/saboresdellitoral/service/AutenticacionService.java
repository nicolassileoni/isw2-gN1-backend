package com.grupo1.saboresdellitoral.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.LoginRequest;
import com.grupo1.saboresdellitoral.dto.LoginResponse;
import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.exception.NoAutorizadoException;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;

@Service
public class AutenticacionService {

	private final UsuarioRepository usuarioRepository;

	public AutenticacionService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		if (request == null || request.username() == null || request.username().isBlank()
				|| request.password() == null || request.password().isEmpty()) {
			throw new NoAutorizadoException("Credenciales inválidas");
		}

		Usuario usuario = usuarioRepository.findByUsername(request.username().trim())
				.orElseThrow(() -> new NoAutorizadoException("Credenciales inválidas"));

		if (!Boolean.TRUE.equals(usuario.getActivo()) || !usuario.getPassword().equals(request.password())) {
			throw new NoAutorizadoException("Credenciales inválidas");
		}

		return new LoginResponse(usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(), usuario.getRol());
	}
}
