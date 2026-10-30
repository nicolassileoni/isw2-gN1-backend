package com.grupo1.saboresdellitoral;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.ActualizarUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.CambioActivoRequest;
import com.grupo1.saboresdellitoral.entity.Rol;
import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void rutasPublicasFuncionanSinToken() throws Exception {
		mockMvc.perform(get("/api/locales"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/categorias"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/pedidos/seguimiento/NOEXISTE"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/locales/999999/carta"))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/pedidos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rutasProtegidasSinTokenDevuelven401() throws Exception {
		mockMvc.perform(get("/api/pedidos"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value("No autorizado"));

		mockMvc.perform(get("/api/repartidores"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/ventas")
				.param("desde", "2026-01-01")
				.param("hasta", "2026-12-31"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenDeOperadorAccedeAPersonalPeroNoAAdmin() throws Exception {
		crearOperador();
		String token = login("operador-test", "Operador2026!");

		mockMvc.perform(get("/api/pedidos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/repartidores")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/usuarios")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void tokenDeAdminAccedeAAdministracion() throws Exception {
		String token = login("admin", "Admin2026!");

		mockMvc.perform(get("/api/usuarios")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/locales/todos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/ventas")
				.param("desde", "2026-01-01")
				.param("hasta", "2026-12-31")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void tokenInvalidoDevuelve401() throws Exception {
		mockMvc.perform(get("/api/pedidos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginDevuelveTokenSinExponerPassword() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new com.grupo1.saboresdellitoral.dto.LoginRequest("admin", "Admin2026!"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void passwordDelAdminEstaHasheadaConBCrypt() {
		Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

		assertNotEquals("Admin2026!", admin.getPassword());
		assertTrue(passwordEncoder.matches("Admin2026!", admin.getPassword()));
	}

	@Test
	void adminNoPuedeDesactivarseNiQuitarseElRolConTokenReal() throws Exception {
		String token = login("admin", "Admin2026!");

		mockMvc.perform(patch("/api/usuarios/1/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(false)))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isConflict());

		mockMvc.perform(put("/api/usuarios/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new ActualizarUsuarioRequest(
						"Administrador de Sabores", Rol.OPERADOR)))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isConflict());
	}

	@Test
	void permitePreflightCorsDesdeExpoWeb() throws Exception {
		mockMvc.perform(options("/api/locales")
				.header(HttpHeaders.ORIGIN, "http://localhost:8081")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
						"http://localhost:8081"));
	}

	private void crearOperador() {
		if (usuarioRepository.findByUsername("operador-test").isPresent()) return;

		Usuario usuario = new Usuario();
		usuario.setUsername("operador-test");
		usuario.setPassword(passwordEncoder.encode("Operador2026!"));
		usuario.setNombreCompleto("Operador de Prueba");
		usuario.setRol(Rol.OPERADOR);
		usuario.setActivo(true);
		usuarioRepository.saveAndFlush(usuario);
	}

	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new com.grupo1.saboresdellitoral.dto.LoginRequest(username, password))))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
	}
}
