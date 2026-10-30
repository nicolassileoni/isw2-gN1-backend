package com.grupo1.saboresdellitoral;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.entity.Repartidor;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;

@WithMockUser(username = "admin", roles = "ADMIN")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class CatalogoTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private RepartidorRepository repartidorRepository;

	@Test
	void listaCategoriasOrdenadasPorNombre() throws Exception {
		mockMvc.perform(get("/api/categorias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5))
				.andExpect(jsonPath("$[0].nombre").value("Bebidas"))
				.andExpect(jsonPath("$[1].nombre").value("Empanadas"))
				.andExpect(jsonPath("$[2].nombre").value("Hamburguesas"))
				.andExpect(jsonPath("$[3].nombre").value("Pizzas"))
				.andExpect(jsonPath("$[4].nombre").value("Postres"));
	}

	@Test
	void noListaLocalesAlInicio() throws Exception {
		mockMvc.perform(get("/api/locales"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void listaSoloLocalesActivosOrdenadosPorNombre() throws Exception {
		crearLocal("Zona Norte", true);
		crearLocal("Centro", true);
		crearLocal("Local Cerrado", false);

		mockMvc.perform(get("/api/locales"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].nombre").value("Centro"))
				.andExpect(jsonPath("$[0].direccion").value("Av. Siempre Viva 123"))
				.andExpect(jsonPath("$[0].telefono").value("3425000000"))
				.andExpect(jsonPath("$[1].nombre").value("Zona Norte"));
	}

	@Test
	void devuelveLaCartaDelLocalActivo() throws Exception {
		Local local = crearLocal("Centro", true);
		Categoria pizzas = categoriaRepository.findByNombre("Pizzas").orElseThrow();
		Categoria bebidas = categoriaRepository.findByNombre("Bebidas").orElseThrow();

		crearProducto(local, pizzas, "Muzzarella", "8 porciones", "2500.00", 5, true);
		crearProducto(local, pizzas, "Fugazzeta", null, "3000.00", 0, true);
		crearProducto(local, pizzas, "Napolitana", null, "2800.00", 4, false);
		crearProducto(local, bebidas, "Agua", null, "800.00", 10, true);

		mockMvc.perform(get("/api/locales/{id}/carta", local.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.localId").value(local.getId()))
				.andExpect(jsonPath("$.localNombre").value("Centro"))
				.andExpect(jsonPath("$.categorias.length()").value(2))
				.andExpect(jsonPath("$.categorias[0].categoriaNombre").value("Bebidas"))
				.andExpect(jsonPath("$.categorias[0].productos.length()").value(1))
				.andExpect(jsonPath("$.categorias[0].productos[0].nombre").value("Agua"))
				.andExpect(jsonPath("$.categorias[1].categoriaNombre").value("Pizzas"))
				.andExpect(jsonPath("$.categorias[1].productos.length()").value(2))
				.andExpect(jsonPath("$.categorias[1].productos[0].nombre").value("Fugazzeta"))
				.andExpect(jsonPath("$.categorias[1].productos[0].stock").value(0))
				.andExpect(jsonPath("$.categorias[1].productos[0].disponible").value(false))
				.andExpect(jsonPath("$.categorias[1].productos[1].nombre").value("Muzzarella"))
				.andExpect(jsonPath("$.categorias[1].productos[1].descripcion").value("8 porciones"))
				.andExpect(jsonPath("$.categorias[1].productos[1].precio").value(2500.00))
				.andExpect(jsonPath("$.categorias[1].productos[1].stock").value(5))
				.andExpect(jsonPath("$.categorias[1].productos[1].disponible").value(true));
	}

	@Test
	void omiteCategoriasSinProductosActivos() throws Exception {
		Local local = crearLocal("Centro", true);
		Categoria pizzas = categoriaRepository.findByNombre("Pizzas").orElseThrow();
		Categoria postres = categoriaRepository.findByNombre("Postres").orElseThrow();

		crearProducto(local, pizzas, "Muzzarella", null, "2500.00", 5, true);
		crearProducto(local, postres, "Flan", null, "900.00", 3, false);

		mockMvc.perform(get("/api/locales/{id}/carta", local.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categorias.length()").value(1))
				.andExpect(jsonPath("$.categorias[0].categoriaNombre").value("Pizzas"));
	}

	@Test
	void cartaDeLocalInactivoDevuelve404() throws Exception {
		Local local = crearLocal("Local Cerrado", false);
		crearProducto(local, categoriaRepository.findByNombre("Pizzas").orElseThrow(), "Muzzarella", null, "2500.00",
				5, true);

		mockMvc.perform(get("/api/locales/{id}/carta", local.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	void cartaDeLocalInexistenteDevuelve404() throws Exception {
		mockMvc.perform(get("/api/locales/{id}/carta", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	void listaRepartidoresActivosOrdenadosPorNombre() throws Exception {
		mockMvc.perform(get("/api/repartidores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].nombreCompleto").value("Carlos Benítez"))
				.andExpect(jsonPath("$[1].nombreCompleto").value("Iván Paredes"))
				.andExpect(jsonPath("$[2].nombreCompleto").value("Micaela Ortiz"));
	}

	@Test
	void noListaRepartidoresInactivos() throws Exception {
		Repartidor carlos = repartidorRepository.findByActivoTrueOrderByNombreCompletoAsc().get(0);
		carlos.setActivo(false);

		mockMvc.perform(get("/api/repartidores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].nombreCompleto").value("Iván Paredes"))
				.andExpect(jsonPath("$[1].nombreCompleto").value("Micaela Ortiz"));
	}

	private Local crearLocal(String nombre, boolean activo) {
		Local local = new Local();
		local.setNombre(nombre);
		local.setDireccion("Av. Siempre Viva 123");
		local.setTelefono("3425000000");
		local.setActivo(activo);
		return localRepository.save(local);
	}

	private Producto crearProducto(Local local, Categoria categoria, String nombre, String descripcion, String precio,
			int stock, boolean activo) {
		Producto producto = new Producto();
		producto.setLocal(local);
		producto.setCategoria(categoria);
		producto.setNombre(nombre);
		producto.setDescripcion(descripcion);
		producto.setPrecio(new BigDecimal(precio));
		producto.setStock(stock);
		producto.setActivo(activo);
		return productoRepository.save(producto);
	}
}
