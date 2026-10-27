package com.grupo1.saboresdellitoral.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.grupo1.saboresdellitoral.entity.Rol;
import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.exception.AccesoDenegadoException;
import com.grupo1.saboresdellitoral.exception.NoAutorizadoException;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;
import com.grupo1.saboresdellitoral.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	private static final String PREFIJO = "Bearer ";

	private final TokenService tokenService;
	private final UsuarioRepository usuarioRepository;

	public AuthInterceptor(TokenService tokenService, UsuarioRepository usuarioRepository) {
		this.tokenService = tokenService;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith(PREFIJO)) {
			throw new NoAutorizadoException("Token de autenticación requerido");
		}

		String token = authorization.substring(PREFIJO.length()).trim();
		Integer usuarioId = tokenService.obtenerUsuarioId(token)
				.orElseThrow(() -> new NoAutorizadoException("Token inválido o vencido"));

		Usuario usuario = usuarioRepository.findById(usuarioId)
				.filter(usuarioEncontrado -> Boolean.TRUE.equals(usuarioEncontrado.getActivo()))
				.orElseThrow(() -> new NoAutorizadoException("Token inválido o vencido"));

		if (usuario.getRol() != Rol.ADMIN) {
			throw new AccesoDenegadoException("No tiene permisos para esta operación");
		}

		request.setAttribute("usuarioId", usuario.getId());
		return true;
	}
}
