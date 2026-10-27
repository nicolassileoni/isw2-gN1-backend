package com.grupo1.saboresdellitoral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
	void loginAdminDevuelveDatosSinPassword() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("admin", "Admin2026!"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.nombreCompleto").value("Administrador de Sabores"))
				.andExpect(jsonPath("$.rol").value("ADMIN"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andExpect(jsonPath("$.token").doesNotExist());
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
	void creaUsuarioYGuardaPasswordSimple() throws Exception {
		CrearUsuarioRequest request = new CrearUsuarioRequest("operador1", "Password123", "Operador Uno",
				Rol.OPERADOR);

		mockMvc.perform(post("/api/usuarios")
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
		assertEquals("Password123", usuario.getPassword());
	}

	@Test
	void rechazaUsernameDuplicado() throws Exception {
		crearOperador("operador1");

		mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CrearUsuarioRequest("operador1", "Password123",
						"Otro Operador", Rol.OPERADOR))))
				.andExpect(status().isConflict());
	}

	@Test
	void rechazaPasswordInvalida() throws Exception {
		mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CrearUsuarioRequest("operador2", "1234567",
						"Operador Dos", Rol.OPERADOR))))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CrearUsuarioRequest("operador3", "1".repeat(61),
						"Operador Tres", Rol.OPERADOR))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizaNombreYRol() throws Exception {
		Integer id = crearOperador("operador1");

		mockMvc.perform(put("/api/usuarios/" + id)
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
	void cambiaPasswordYLoginFunciona() throws Exception {
		Integer id = crearOperador("operador1");

		mockMvc.perform(patch("/api/usuarios/" + id + "/password")
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
	void desactivaYReactivaUsuario() throws Exception {
		Integer id = crearOperador("operador1");

		mockMvc.perform(patch("/api/usuarios/" + id + "/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(false))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(false));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("operador1", "Password123"))))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(patch("/api/usuarios/" + id + "/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(true))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(true));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("operador1", "Password123"))))
				.andExpect(status().isOk());
	}

	@Test
	void listaUsuariosSinPassword() throws Exception {
		crearOperador("operador1");

		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].username").value("admin"))
				.andExpect(jsonPath("$[1].username").value("operador1"))
				.andExpect(jsonPath("$[0].password").doesNotExist())
				.andExpect(jsonPath("$[0].passwordHash").doesNotExist());
	}

	private Integer crearOperador(String username) throws Exception {
		CrearUsuarioRequest request = new CrearUsuarioRequest(username, "Password123", "Operador " + username,
				Rol.OPERADOR);

		MvcResult result = mockMvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
	}

	private Usuario guardarUsuario(String username, String password, String nombreCompleto, Rol rol, boolean activo) {
		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(password);
		usuario.setNombreCompleto(nombreCompleto);
		usuario.setRol(rol);
		usuario.setActivo(activo);
		return usuarioRepository.saveAndFlush(usuario);
	}
}
