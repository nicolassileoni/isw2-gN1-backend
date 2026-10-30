package com.grupo1.saboresdellitoral.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;
	private final UsuarioRepository usuarioRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
		this.jwtService = jwtService;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(BEARER_PREFIX.length()).trim();

		try {
			String username = jwtService.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

				if (usuario != null && Boolean.TRUE.equals(usuario.getActivo())
						&& jwtService.isTokenValid(token, usuario)) {
					var authentication = new UsernamePasswordAuthenticationToken(
							usuario.getUsername(),
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} catch (JwtException | IllegalArgumentException exception) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}
