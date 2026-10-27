package com.grupo1.saboresdellitoral.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.dto.LoginRequest;
import com.grupo1.saboresdellitoral.dto.LoginResponse;
import com.grupo1.saboresdellitoral.service.AutenticacionService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AutenticacionService autenticacionService;

	public AuthController(AutenticacionService autenticacionService) {
		this.autenticacionService = autenticacionService;
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		return autenticacionService.login(request);
	}
}
