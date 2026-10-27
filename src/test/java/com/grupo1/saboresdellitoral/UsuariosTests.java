package com.grupo1.saboresdellitoral;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.grupo1.saboresdellitoral.dto.ActualizarUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.CambioActivoRequest;
import com.grupo1.saboresdellitoral.dto.CambiarPasswordRequest;
import com.grupo1.saboresdellitoral.dto.CrearUsuarioRequest;
import com.grupo1.saboresdellitoral.dto.LoginRequest;
import com.grupo1.saboresdellitoral.entity.Rol;
import com.grupo1.saboresdellitoral.entity.Usuario;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuariosTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	void loginAdminDevuelveTokenNombreYRolSinPassword() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("admin", "Admin2026!"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.nombreCompleto").value("Administrador de Sabores"))
				.andExpect(jsonPath("$.rol").value("ADMIN"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void loginConCredencialesInvalidasDevuelve401() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("admin", "Incorrecta123"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("noexiste", "Password123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void usuarioInactivoNoPuedeIngresar() throws Exception {
		guardarUsuario("inactivo", "Password123", "Usuario Inactivo", Rol.OPERADOR, false);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("inactivo", "Password123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void endpointUsuariosRequiereToken() throws Exception {
		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void creaUsuarioYGuardaPasswordHasheada() throws Exception {
		String adminToken = loginAdmin();
		CrearUsuarioRequest request = new CrearUsuarioRequest("operador1", "Password123", "Operador Uno",
				Rol.OPERADOR);

		mockMvc.perform(post("/api/usuarios")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.username").value("operador1"))
				.andExpect(jsonPath("$.nombreCompleto").value("Operador Uno"))
				.andExpect(jsonPath("$.rol").value("OPERADOR"))
				.andExpect(jsonPath("$.activo").value(true))
				.andExpect(jsonPath("$.fechaAlta").exists())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		Usuario usuario = usuarioRepository.findByUsername("operador1").orElseThrow();
		assertNotEquals("Password123", usuario.getPasswordHash());
		assertTrue(usuario.getPasswordHash().startsWith("$2"));
		assertTrue(BCrypt.checkpw("Password123", usuario.getPasswordHash()));
	}

	@Test
	void rechazaUsernameDuplicado() throws Exception {
		String adminToken = loginAdmin();
		crearOperador(adminToken, "operador1");

		mockMvc.perform(post("/api/usuarios")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CrearUsuarioRequest("operador1", "Password123",
						"Otro Operador", Rol.OPERADOR))))
				.andExpect(status().isConflict());
	}

	@Test
	void rechazaPasswordCorta() throws Exception {
		String adminToken = loginAdmin();

		mockMvc.perform(post("/api/usuarios")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CrearUsuarioRequest("operador2", "1234567",
						"Operador Dos", Rol.OPERADOR))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizaNombreYRol() throws Exception {
		String adminToken = loginAdmin();
		Integer id = crearOperador(adminToken, "operador1");

		mockMvc.perform(put("/api/usuarios/" + id)
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new ActualizarUsuarioRequest("Operador Actualizado",
						Rol.ADMIN))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("operador1"))
				.andExpect(jsonPath("$.nombreCompleto").value("Operador Actualizado"))
				.andExpect(jsonPath("$.rol").value("ADMIN"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void adminNoPuedeQuitarseRolAdmin() throws Exception {
		String adminToken = loginAdmin();
		Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

		mockMvc.perform(put("/api/usuarios/" + admin.getId())
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new ActualizarUsuarioRequest(admin.getNombreCompleto(),
						Rol.OPERADOR))))
				.andExpect(status().isConflict());
	}

	@Test
	void adminNoPuedeDesactivarse() throws Exception {
		String adminToken = loginAdmin();
		Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

		mockMvc.perform(patch("/api/usuarios/" + admin.getId() + "/activo")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(false))))
				.andExpect(status().isConflict());
	}

	@Test
	void cambiaPasswordYLoginFunciona() throws Exception {
		String adminToken = loginAdmin();
		Integer id = crearOperador(adminToken, "operador1");

		mockMvc.perform(patch("/api/usuarios/" + id + "/password")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambiarPasswordRequest("NuevaClave123"))))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("operador1", "Password123"))))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("operador1", "NuevaClave123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rol").value("OPERADOR"));
	}

	@Test
	void desactivaUsuarioYNoPuedeIngresar() throws Exception {
		String adminToken = loginAdmin();
		Integer id = crearOperador(adminToken, "operador1");
		String operadorToken = login("operador1", "Password123");

		mockMvc.perform(patch("/api/usuarios/" + id + "/activo")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(false))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(false));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("operador1", "Password123"))))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(operadorToken)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void operadorNoPuedeAccederAUsuarios() throws Exception {
		String adminToken = loginAdmin();
		crearOperador(adminToken, "operador1");
		String operadorToken = login("operador1", "Password123");

		mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(operadorToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void listaUsuariosSinPassword() throws Exception {
		String adminToken = loginAdmin();
		crearOperador(adminToken, "operador1");

		mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].username").value("admin"))
				.andExpect(jsonPath("$[1].username").value("operador1"))
				.andExpect(jsonPath("$[0].password").doesNotExist())
				.andExpect(jsonPath("$[0].passwordHash").doesNotExist());
	}

	private String loginAdmin() throws Exception {
		return login("admin", "Admin2026!");
	}

	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
	}

	private Integer crearOperador(String adminToken, String username) throws Exception {
		CrearUsuarioRequest request = new CrearUsuarioRequest(username, "Password123", "Operador " + username,
				Rol.OPERADOR);

		MvcResult result = mockMvc.perform(post("/api/usuarios")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}

	private Usuario guardarUsuario(String username, String password, String nombreCompleto, Rol rol, boolean activo) {
		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
		usuario.setNombreCompleto(nombreCompleto);
		usuario.setRol(rol);
		usuario.setActivo(activo);
		return usuarioRepository.saveAndFlush(usuario);
	}
}
