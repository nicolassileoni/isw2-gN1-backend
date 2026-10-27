package com.grupo1.saboresdellitoral;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.grupo1.saboresdellitoral.dto.ClientePedidoRequest;
import com.grupo1.saboresdellitoral.dto.CrearPedidoRequest;
import com.grupo1.saboresdellitoral.dto.ItemPedidoRequest;
import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoCreacionTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Test
	void creaPedidoValidoYDescuentaStock() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto pizza = guardarProducto(local, "Muzzarella", "2500.00", 10, true);
		Producto bebida = guardarProducto(local, "Agua", "1500.00", 5, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000",
				item(pizza, 2), item(bebida, 1)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/api/pedidos/seguimiento/[A-Z0-9]{8}")))
				.andExpect(jsonPath("$.codigoSeguimiento", matchesPattern("[A-Z0-9]{8}")))
				.andExpect(jsonPath("$.total").value(8000.00))
				.andExpect(jsonPath("$.estado").value("RECIBIDO"))
				.andExpect(jsonPath("$.fechaHora").exists())
				.andExpect(jsonPath("$.id").doesNotExist());

		assertEquals(8, productoRepository.findById(pizza.getId()).orElseThrow().getStock());
		assertEquals(4, productoRepository.findById(bebida.getId()).orElseThrow().getStock());

		Cliente cliente = clienteRepository.findByTelefono("3425000000").orElseThrow();
		assertEquals("Ana Pérez", cliente.getNombre());

		Pedido pedido = pedidoRepository.findAll().get(0);
		assertEquals(EstadoPedido.RECIBIDO, pedido.getEstado());
		assertEquals(0, new BigDecimal("6500.00").compareTo(pedido.getSubtotal()));
		assertEquals(0, new BigDecimal("0.00").compareTo(pedido.getDescuento()));
		assertEquals(0, new BigDecimal("1500.00").compareTo(pedido.getCostoEnvio()));
		assertEquals(0, new BigDecimal("8000.00").compareTo(pedido.getTotal()));
		assertEquals(2, pedido.getItems().size());
		assertTrue(pedido.getItems().stream()
				.anyMatch(item -> item.getPrecioUnitario().compareTo(new BigDecimal("2500.00")) == 0));
	}

	@Test
	void reutilizaClienteExistentePorTelefono() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);
		Cliente existente = guardarCliente("Nombre Original", "3425000000");

		postPedido(pedido(local.getId(), "San Martín 123", "Otro Nombre", "3425000000", item(producto, 1)))
				.andExpect(status().isCreated());

		assertEquals(1, clienteRepository.findAll().size());
		Cliente cliente = clienteRepository.findByTelefono("3425000000").orElseThrow();
		assertEquals(existente.getId(), cliente.getId());
		assertEquals("Nombre Original", cliente.getNombre());
	}

	@Test
	void noAplicaDescuentoConSubtotalExactoDeVeinteMil() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Combo", "20000.00", 5, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(21500.00));

		Pedido pedido = pedidoRepository.findAll().get(0);
		assertEquals(0, new BigDecimal("0.00").compareTo(pedido.getDescuento()));
	}

	@Test
	void aplicaDescuentoCuandoSubtotalSuperaVeinteMil() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Combo", "20000.01", 5, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(19500.01));

		Pedido pedido = pedidoRepository.findAll().get(0);
		assertEquals(0, new BigDecimal("20000.01").compareTo(pedido.getSubtotal()));
		assertEquals(0, new BigDecimal("2000.00").compareTo(pedido.getDescuento()));
	}

	@Test
	void cobraEnvioFijo() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Empanada", "1000.00", 5, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(2500.00));

		Pedido pedido = pedidoRepository.findAll().get(0);
		assertEquals(0, new BigDecimal("1500.00").compareTo(pedido.getCostoEnvio()));
	}

	@Test
	void rechazaProductoRepetido() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000",
				item(producto, 1), item(producto, 2)))
				.andExpect(status().isBadRequest());

		assertEquals(10, productoRepository.findById(producto.getId()).orElseThrow().getStock());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaCantidadCeroONegativa() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 0)))
				.andExpect(status().isBadRequest());
		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, -1)))
				.andExpect(status().isBadRequest());

		assertEquals(10, productoRepository.findById(producto.getId()).orElseThrow().getStock());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaTelefonoInvalido() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		for (String telefono : List.of("1234567", "1234567890123456", "3425ABC000", "3425 000000",
				"+3425000000")) {
			postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", telefono, item(producto, 1)))
					.andExpect(status().isBadRequest());
		}

		assertTrue(clienteRepository.findAll().isEmpty());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaNombreODireccionVacios() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(local.getId(), "   ", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isBadRequest());
		postPedido(pedido(local.getId(), "San Martín 123", "   ", "3425000000", item(producto, 1)))
				.andExpect(status().isBadRequest());

		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaNombreODireccionDemasiadoLargos() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(local.getId(), "a".repeat(151), "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isBadRequest());
		postPedido(pedido(local.getId(), "San Martín 123", "a".repeat(81), "3425000000", item(producto, 1)))
				.andExpect(status().isBadRequest());

		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaItemsVacio() throws Exception {
		Local local = guardarLocal("Centro", true);

		postPedido(new CrearPedidoRequest(local.getId(), "San Martín 123",
				new ClientePedidoRequest("Ana Pérez", "3425000000"), List.of()))
				.andExpect(status().isBadRequest());
		postPedido(new CrearPedidoRequest(local.getId(), "San Martín 123",
				new ClientePedidoRequest("Ana Pérez", "3425000000"), null))
				.andExpect(status().isBadRequest());

		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaCamposObligatoriosNulos() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(new CrearPedidoRequest(null, "San Martín 123",
				new ClientePedidoRequest("Ana Pérez", "3425000000"), List.of(item(producto, 1))))
				.andExpect(status().isBadRequest());
		postPedido(new CrearPedidoRequest(local.getId(), "San Martín 123", null, List.of(item(producto, 1))))
				.andExpect(status().isBadRequest());
		postPedido(new CrearPedidoRequest(local.getId(), "San Martín 123",
				new ClientePedidoRequest("Ana Pérez", "3425000000"),
				List.of(new ItemPedidoRequest(null, 1))))
				.andExpect(status().isBadRequest());
		postPedido(new CrearPedidoRequest(local.getId(), "San Martín 123",
				new ClientePedidoRequest("Ana Pérez", "3425000000"),
				List.of(new ItemPedidoRequest(producto.getId(), null))))
				.andExpect(status().isBadRequest());

		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaLocalInexistente() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(999999, "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isBadRequest());

		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaProductoInexistente() throws Exception {
		Local local = guardarLocal("Centro", true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000",
				new ItemPedidoRequest(999999, 1)))
				.andExpect(status().isBadRequest());

		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaLocalInactivo() throws Exception {
		Local local = guardarLocal("Centro", false);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isConflict());

		assertEquals(10, productoRepository.findById(producto.getId()).orElseThrow().getStock());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaProductoInactivo() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10, false);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isConflict());

		assertEquals(10, productoRepository.findById(producto.getId()).orElseThrow().getStock());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaProductoDeOtroLocal() throws Exception {
		Local local = guardarLocal("Centro", true);
		Local otroLocal = guardarLocal("Zona Norte", true);
		Producto producto = guardarProducto(otroLocal, "Muzzarella", "2500.00", 10, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000", item(producto, 1)))
				.andExpect(status().isConflict());

		assertEquals(10, productoRepository.findById(producto.getId()).orElseThrow().getStock());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void rechazaStockInsuficienteSinDescontarNingunProducto() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto suficiente = guardarProducto(local, "Muzzarella", "2500.00", 10, true);
		Producto insuficiente = guardarProducto(local, "Agua", "1500.00", 1, true);

		postPedido(pedido(local.getId(), "San Martín 123", "Ana Pérez", "3425000000",
				item(suficiente, 1), item(insuficiente, 2)))
				.andExpect(status().isConflict());

		assertEquals(10, productoRepository.findById(suficiente.getId()).orElseThrow().getStock());
		assertEquals(1, productoRepository.findById(insuficiente.getId()).orElseThrow().getStock());
		assertTrue(pedidoRepository.findAll().isEmpty());
	}

	@Test
	void ignoraMontosYCamposExtra() throws Exception {
		Local local = guardarLocal("Centro", true);
		Producto producto = guardarProducto(local, "Muzzarella", "1000.00", 5, true);

		String json = """
				{
				  "localId": %d,
				  "direccionEntrega": "San Martín 123",
				  "cliente": {"nombre": "Ana Pérez", "telefono": "3425000000"},
				  "items": [{"productoId": %d, "cantidad": 1}],
				  "subtotal": 1,
				  "descuento": 1,
				  "costoEnvio": 1,
				  "total": 1,
				  "estado": "ENTREGADO",
				  "id": 999
				}
				""".formatted(local.getId(), producto.getId());

		mockMvc.perform(post("/api/pedidos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(2500.00))
				.andExpect(jsonPath("$.estado").value("RECIBIDO"))
				.andExpect(jsonPath("$.id").doesNotExist());

		assertEquals(0, new BigDecimal("2500.00")
				.compareTo(pedidoRepository.findAll().get(0).getTotal()));
	}

	private Local guardarLocal(String nombre, boolean activo) {
		Local local = new Local();
		local.setNombre(nombre);
		local.setActivo(activo);
		return localRepository.saveAndFlush(local);
	}

	private Producto guardarProducto(Local local, String nombre, String precio, int stock, boolean activo) {
		Categoria categoria = categoriaRepository.findByNombre("Pizzas").orElseThrow();
		Producto producto = new Producto();
		producto.setLocal(local);
		producto.setCategoria(categoria);
		producto.setNombre(nombre);
		producto.setPrecio(new BigDecimal(precio));
		producto.setStock(stock);
		producto.setActivo(activo);
		return productoRepository.saveAndFlush(producto);
	}

	private Cliente guardarCliente(String nombre, String telefono) {
		Cliente cliente = new Cliente();
		cliente.setNombre(nombre);
		cliente.setTelefono(telefono);
		return clienteRepository.saveAndFlush(cliente);
	}

	private CrearPedidoRequest pedido(Integer localId, String direccionEntrega, String nombre, String telefono,
			ItemPedidoRequest... items) {
		return new CrearPedidoRequest(localId, direccionEntrega, new ClientePedidoRequest(nombre, telefono),
				List.of(items));
	}

	private ItemPedidoRequest item(Producto producto, int cantidad) {
		return new ItemPedidoRequest(producto.getId(), cantidad);
	}

	private ResultActions postPedido(CrearPedidoRequest request) throws Exception {
		return mockMvc.perform(post("/api/pedidos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));
	}
}
