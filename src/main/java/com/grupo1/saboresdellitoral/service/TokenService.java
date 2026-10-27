package com.grupo1.saboresdellitoral.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

	private static final Duration DURACION = Duration.ofHours(8);

	private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

	public String crear(Integer usuarioId) {
		String token = UUID.randomUUID().toString();
		sesiones.put(token, new Sesion(usuarioId, Instant.now().plus(DURACION)));
		return token;
	}

	public Optional<Integer> obtenerUsuarioId(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}

		Sesion sesion = sesiones.get(token);
		if (sesion == null) {
			return Optional.empty();
		}

		if (sesion.expiraEn().isBefore(Instant.now())) {
			sesiones.remove(token);
			return Optional.empty();
		}

		return Optional.of(sesion.usuarioId());
	}

	private record Sesion(Integer usuarioId, Instant expiraEn) {
	}
}
