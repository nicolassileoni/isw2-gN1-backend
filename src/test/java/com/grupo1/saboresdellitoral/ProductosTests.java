package com.grupo1.saboresdellitoral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.ItemPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.ItemPedidoRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;

@WithMockUser(username = "admin", roles = "ADMIN")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductosTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private ItemPedidoRepository itemPedidoRepository;

	@Test
	void creaProductoActivo() throws Exception {
		Local local = crearLocal("Pizzería Centro");
		Categoria categoria = categoriaRepository.findByNombre("Pizzas").orElseThrow();

		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(local.getId(), categoria.getId(), "  Muzzarella  ", " Clásica ", "2500.555", 8)))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", startsWith("/api/productos/")))
			.andExpect(jsonPath("$.nombre").value("Muzzarella"))
			.andExpect(jsonPath("$.descripcion").value("Clásica"))
			.andExpect(jsonPath("$.precio").value(2500.56))
			.andExpect(jsonPath("$.stock").value(8))
			.andExpect(jsonPath("$.activo").value(true));
	}

	@Test
	void actualizaProducto() throws Exception {
		Producto producto = crearProducto("Muzzarella", "2500.00", 5, true);
		Categoria categoria = producto.getCategoria();

		mockMvc.perform(put("/api/productos/{id}", producto.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(producto.getLocal().getId(), categoria.getId(), "Napolitana", null, "3200", 12)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Napolitana"))
			.andExpect(jsonPath("$.descripcion").doesNotExist())
			.andExpect(jsonPath("$.precio").value(3200.00))
			.andExpect(jsonPath("$.stock").value(12));
	}

	@Test
	void desactivaYReactivaProducto() throws Exception {
		Producto producto = crearProducto("Muzzarella", "2500.00", 5, true);

		mockMvc.perform(patch("/api/productos/{id}/activo", producto.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"activo\":false}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activo").value(false));

		mockMvc.perform(patch("/api/productos/{id}/activo", producto.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"activo\":true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activo").value(true));
	}

	@Test
	void rechazaPrecioNegativo() throws Exception {
		Producto producto = crearProducto("Muzzarella", "2500.00", 5, true);

		mockMvc.perform(put("/api/productos/{id}", producto.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(producto.getLocal().getId(), producto.getCategoria().getId(), "Muzzarella", null,
						"-1", 5)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaStockNegativo() throws Exception {
		Producto producto = crearProducto("Muzzarella", "2500.00", 5, true);

		mockMvc.perform(put("/api/productos/{id}", producto.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(producto.getLocal().getId(), producto.getCategoria().getId(), "Muzzarella", null,
						"2500", -1)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaLocalInexistente() throws Exception {
		Categoria categoria = categoriaRepository.findByNombre("Pizzas").orElseThrow();

		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(999999, categoria.getId(), "Muzzarella", null, "2500", 5)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaCategoriaInexistente() throws Exception {
		Local local = crearLocal("Pizzería Centro");

		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(local.getId(), 999999, "Muzzarella", null, "2500", 5)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void devuelveNotFoundParaProductoInexistente() throws Exception {
		mockMvc.perform(patch("/api/productos/{id}/activo", 999999)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"activo\":false}"))
			.andExpect(status().isNotFound());
	}

	@Test
	void listadoPorLocalIncluyeProductosInactivos() throws Exception {
		Local local = crearLocal("Pizzería Centro");
		crearProducto(local, "Muzzarella", "2500.00", 5, true);
		crearProducto(local, "Napolitana", "2800.00", 3, false);

		mockMvc.perform(get("/api/productos").param("localId", local.getId().toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].nombre").value("Muzzarella"))
			.andExpect(jsonPath("$[1].nombre").value("Napolitana"))
			.andExpect(jsonPath("$[1].activo").value(false));
	}

	@Test
	void listadoSinLocalDevuelveTodosLosProductos() throws Exception {
		crearProducto(crearLocal("Pizzería Centro"), "Muzzarella", "2500.00", 5, true);
		crearProducto(crearLocal("Pizzería Norte"), "Hamburguesa", "3000.00", 4, true);

		mockMvc.perform(get("/api/productos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void cartaPublicaOcultaProductosInactivos() throws Exception {
		Local local = crearLocal("Pizzería Centro");
		crearProducto(local, "Muzzarella", "2500.00", 5, true);
		crearProducto(local, "Napolitana", "2800.00", 3, false);

		mockMvc.perform(get("/api/locales/{id}/carta", local.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categorias[0].productos.length()").value(1))
			.andExpect(jsonPath("$.categorias[0].productos[0].nombre").value("Muzzarella"));
	}

	@Test
	void cambioDePrecioNoModificaItemsExistentes() throws Exception {
		Producto producto = crearProducto("Muzzarella", "2500.00", 5, true);
		ItemPedido item = crearItemPedido(producto, "2500.00");

		mockMvc.perform(put("/api/productos/{id}", producto.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(productoJson(producto.getLocal().getId(), producto.getCategoria().getId(), "Muzzarella", null,
						"3200.00", 5)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.precio").value(3200.00));

		assertEquals(new BigDecimal("2500.00"), itemPedidoRepository.findById(item.getId()).orElseThrow().getPrecioUnitario());
	}

	private Local crearLocal(String nombre) {
		Local local = new Local();
		local.setNombre(nombre);
		local.setActivo(true);
		return localRepository.save(local);
	}

	private Producto crearProducto(String nombre, String precio, int stock, boolean activo) {
		return crearProducto(crearLocal("Local " + nombre), nombre, precio, stock, activo);
	}

	private Producto crearProducto(Local local, String nombre, String precio, int stock, boolean activo) {
		Producto producto = new Producto();
		producto.setLocal(local);
		producto.setCategoria(categoriaRepository.findByNombre("Pizzas").orElseThrow());
		producto.setNombre(nombre);
		producto.setPrecio(new BigDecimal(precio));
		producto.setStock(stock);
		producto.setActivo(activo);
		return productoRepository.save(producto);
	}

	private ItemPedido crearItemPedido(Producto producto, String precioUnitario) {
		Cliente cliente = new Cliente();
		cliente.setNombre("Cliente de prueba");
		cliente.setTelefono("3425000000");
		clienteRepository.save(cliente);

		Pedido pedido = new Pedido();
		pedido.setCodigoSeguimiento("ABCD1234");
		pedido.setCliente(cliente);
		pedido.setLocal(producto.getLocal());
		pedido.setDireccionEntrega("Calle 123");
		pedido.setSubtotal(new BigDecimal("2500.00"));
		pedido.setDescuento(BigDecimal.ZERO);
		pedido.setCostoEnvio(new BigDecimal("1500.00"));
		pedido.setTotal(new BigDecimal("4000.00"));
		pedidoRepository.save(pedido);

		ItemPedido item = new ItemPedido();
		item.setPedido(pedido);
		item.setProducto(producto);
		item.setCantidad(1);
		item.setPrecioUnitario(new BigDecimal(precioUnitario));
		return itemPedidoRepository.save(item);
	}

	private String productoJson(Integer localId, Integer categoriaId, String nombre, String descripcion, String precio,
			int stock) {
		String descripcionJson = descripcion == null ? "null" : "\"" + descripcion + "\"";
		return "{\"localId\":" + localId + ",\"categoriaId\":" + categoriaId + ",\"nombre\":\"" + nombre
				+ "\",\"descripcion\":" + descripcionJson + ",\"precio\":" + precio + ",\"stock\":" + stock + "}";
	}
}
