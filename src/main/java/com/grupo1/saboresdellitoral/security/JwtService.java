package com.grupo1.saboresdellitoral.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.grupo1.saboresdellitoral.entity.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(Usuario usuario) {
		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + expirationMs);

		return Jwts.builder()
				.subject(usuario.getUsername())
				.claim("rol", usuario.getRol().name())
				.issuedAt(issuedAt)
				.expiration(expiration)
				.signWith(key)
				.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, Usuario usuario) {
		Claims claims = parseClaims(token);
		return usuario.getUsername().equals(claims.getSubject())
				&& claims.getExpiration().after(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
