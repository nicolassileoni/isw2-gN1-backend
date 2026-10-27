package com.grupo1.saboresdellitoral.service;

import org.mindrot.jbcrypt.BCrypt;
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
	private final TokenService tokenService;

	public AutenticacionService(UsuarioRepository usuarioRepository, TokenService tokenService) {
		this.usuarioRepository = usuarioRepository;
		this.tokenService = tokenService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		if (request == null || request.username() == null || request.username().isBlank()
				|| request.password() == null || request.password().isEmpty()) {
			throw new NoAutorizadoException("Credenciales inválidas");
		}

		Usuario usuario = usuarioRepository.findByUsername(request.username().trim())
				.orElseThrow(() -> new NoAutorizadoException("Credenciales inválidas"));

		if (!Boolean.TRUE.equals(usuario.getActivo()) || !passwordValida(request.password(), usuario.getPasswordHash())) {
			throw new NoAutorizadoException("Credenciales inválidas");
		}

		String token = tokenService.crear(usuario.getId());
		return new LoginResponse(token, usuario.getNombreCompleto(), usuario.getRol());
	}

	private boolean passwordValida(String password, String passwordHash) {
		try {
			return BCrypt.checkpw(password, passwordHash);
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}
}
