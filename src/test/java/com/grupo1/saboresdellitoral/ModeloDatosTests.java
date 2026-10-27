package com.grupo1.saboresdellitoral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;
import com.grupo1.saboresdellitoral.repository.UsuarioRepository;

@SpringBootTest
class ModeloDatosTests {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private RepartidorRepository repartidorRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	void cargaLosDatosIniciales() {
		assertEquals(5L, categoriaRepository.count());
		assertEquals(3L, repartidorRepository.count());
		assertTrue(usuarioRepository.findByUsername("admin").isPresent());
	}
}
