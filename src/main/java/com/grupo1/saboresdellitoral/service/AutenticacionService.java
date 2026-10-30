package com.grupo1.saboresdellitoral.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.LoginRequest;
import com.grupo1.saboresdellitoral.dto.LoginResponse;
import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.exception.NoAutorizadoException;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;
import com.grupo1.saboresdellitoral.security.JwtService;

@Service
public class AutenticacionService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AutenticacionService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		if (request == null || request.username() == null || request.username().isBlank()
				|| request.password() == null || request.password().isEmpty()) {
			throw new NoAutorizadoException("Credenciales inválidas");
		}

		Usuario usuario = usuarioRepository.findByUsername(request.username().trim())
				.orElseThrow(() -> new NoAutorizadoException("Credenciales inválidas"));

		if (!Boolean.TRUE.equals(usuario.getActivo())
				|| !passwordEncoder.matches(request.password(), usuario.getPassword())) {
			throw new NoAutorizadoException("Credenciales inválidas");
		}

		String token = jwtService.generateToken(usuario);
		return new LoginResponse(usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(), usuario.getRol(),
				token);
	}
}
