package com.grupo1.saboresdellitoral;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo1.saboresdellitoral.exception.ManejadorGlobalDeExcepciones;
import com.grupo1.saboresdellitoral.exception.ReglaDeNegocioException;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;

class ManejoErroresIntegrationTests {

	private MockMvc mockMvc;

	@BeforeEach
	void configurarMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ControladorPrueba())
			.setControllerAdvice(new ManejadorGlobalDeExcepciones())
			.build();
	}

	@Test
	void devuelveBadRequestParaSolicitudInvalida() throws Exception {
		mockMvc.perform(get("/prueba/error/solicitud"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.mensaje").value("Los datos son inválidos."))
			.andExpect(jsonPath("$.detalles").isArray());
	}

	@Test
	void devuelveNotFoundParaRecursoInexistente() throws Exception {
		mockMvc.perform(get("/prueba/error/recurso"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensaje").value("No se encontró el recurso."));
	}

	@Test
	void devuelveConflictParaReglaDeNegocio() throws Exception {
		mockMvc.perform(get("/prueba/error/regla"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.mensaje").value("Existe un conflicto de negocio."));
	}

	@Test
	void devuelveConflictParaErrorDeIntegridad() throws Exception {
		mockMvc.perform(get("/prueba/error/integridad"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.mensaje").value("No se pudo completar la operación."));
	}

	@Test
	void devuelveBadRequestParaJsonMalformado() throws Exception {
		mockMvc.perform(post("/prueba/cuerpo")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.mensaje").value("La solicitud contiene datos inválidos."));
	}

	@Test
	void devuelveBadRequestParaTipoInvalidoEnLaUrl() throws Exception {
		mockMvc.perform(get("/prueba/numero/invalido"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.mensaje").value("La solicitud contiene datos inválidos."));
	}

	@Test
	void devuelveBadRequestParaParametroObligatorioAusente() throws Exception {
		mockMvc.perform(get("/prueba/parametro"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.mensaje").value("La solicitud contiene datos inválidos."));
	}

	@Test
	void devuelveMensajeGenericoParaErroresInesperados() throws Exception {
		mockMvc.perform(get("/prueba/error/inesperado"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado."));
	}

	@RestController
	static class ControladorPrueba {

		@GetMapping("/prueba/error/{tipo}")
		void lanzarError(@PathVariable String tipo) {
			switch (tipo) {
				case "solicitud" -> throw new SolicitudInvalidaException("Los datos son inválidos.");
				case "recurso" -> throw new RecursoNoEncontradoException("No se encontró el recurso.");
				case "regla" -> throw new ReglaDeNegocioException("Existe un conflicto de negocio.");
				case "integridad" -> throw new DataIntegrityViolationException("Error de integridad");
				default -> throw new IllegalStateException();
			}
		}

		@GetMapping("/prueba/numero/{numero}")
		void recibirNumero(@PathVariable Integer numero) {
		}

		@GetMapping("/prueba/parametro")
		void recibirParametro(@RequestParam String valor) {
		}

		@PostMapping("/prueba/cuerpo")
		void recibirCuerpo(@RequestBody EntradaPrueba entrada) {
		}
	}

	private record EntradaPrueba(String nombre) {
	}
}
