package com.grupo1.saboresdellitoral;

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
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.grupo1.saboresdellitoral.dto.CambioActivoRequest;
import com.grupo1.saboresdellitoral.dto.LocalRequest;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.repository.LocalRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocalesTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private LocalRepository localRepository;

	@Test
	void creaUnLocal() throws Exception {
		LocalRequest request = new LocalRequest("Lo de Juan", "San Martín 123", "3425000000");

		mockMvc.perform(post("/api/locales")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nombre").value("Lo de Juan"))
				.andExpect(jsonPath("$.direccion").value("San Martín 123"))
				.andExpect(jsonPath("$.telefono").value("3425000000"))
				.andExpect(jsonPath("$.activo").value(true));
	}

	@Test
	void recortaEspaciosDeLosCampos() throws Exception {
		LocalRequest request = new LocalRequest("  Lo de Juan  ", "  San Martín 123  ", "  3425000000  ");

		mockMvc.perform(post("/api/locales")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nombre").value("Lo de Juan"))
				.andExpect(jsonPath("$.direccion").value("San Martín 123"))
				.andExpect(jsonPath("$.telefono").value("3425000000"));
	}

	@Test
	void rechazaNombreVacio() throws Exception {
		LocalRequest request = new LocalRequest("   ", null, null);

		mockMvc.perform(post("/api/locales")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaNombreMuyLargo() throws Exception {
		LocalRequest request = new LocalRequest("a".repeat(81), null, null);

		mockMvc.perform(post("/api/locales")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaTelefonoMuyLargo() throws Exception {
		LocalRequest request = new LocalRequest("Lo de Juan", null, "1".repeat(16));

		mockMvc.perform(post("/api/locales")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaNombreDuplicado() throws Exception {
		guardarLocal("Lo de Juan");
		LocalRequest request = new LocalRequest("Lo de Juan", null, null);

		mockMvc.perform(post("/api/locales")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void actualizaUnLocal() throws Exception {
		Local local = guardarLocal("Lo de Juan");
		LocalRequest request = new LocalRequest("Lo de Juan actualizado", "Belgrano 456", "3425111111");

		mockMvc.perform(put("/api/locales/" + local.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Lo de Juan actualizado"))
				.andExpect(jsonPath("$.direccion").value("Belgrano 456"))
				.andExpect(jsonPath("$.telefono").value("3425111111"))
				.andExpect(jsonPath("$.activo").value(true));
	}

	@Test
	void rechazaActualizarConNombreDuplicado() throws Exception {
		Local primero = guardarLocal("Primero");
		guardarLocal("Segundo");
		LocalRequest request = new LocalRequest("Segundo", null, null);

		mockMvc.perform(put("/api/locales/" + primero.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void actualizarInexistenteDevuelveNotFound() throws Exception {
		LocalRequest request = new LocalRequest("Lo de Juan", null, null);

		mockMvc.perform(put("/api/locales/999999")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	void cambiarActivoInexistenteDevuelveNotFound() throws Exception {
		mockMvc.perform(patch("/api/locales/999999/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(false))))
				.andExpect(status().isNotFound());
	}

	@Test
	void desactivaYReactivaUnLocal() throws Exception {
		Local local = guardarLocal("Lo de Juan");

		mockMvc.perform(patch("/api/locales/" + local.getId() + "/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(false))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(false));

		mockMvc.perform(get("/api/locales/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Lo de Juan"))
				.andExpect(jsonPath("$[0].activo").value(false));

		mockMvc.perform(patch("/api/locales/" + local.getId() + "/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CambioActivoRequest(true))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(true));
	}

	@Test
	void rechazaActivoNulo() throws Exception {
		Local local = guardarLocal("Lo de Juan");

		mockMvc.perform(patch("/api/locales/" + local.getId() + "/activo")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listaTodosLosLocalesOrdenadosPorNombre() throws Exception {
		guardarLocal("Zeta");
		guardarLocal("Alfa");

		mockMvc.perform(get("/api/locales/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].nombre").value("Alfa"))
				.andExpect(jsonPath("$[1].nombre").value("Zeta"));
	}

	private Local guardarLocal(String nombre) {
		Local local = new Local();
		local.setNombre(nombre);
		local.setActivo(true);
		return localRepository.saveAndFlush(local);
	}
}
