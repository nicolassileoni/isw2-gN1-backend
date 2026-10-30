package com.grupo1.saboresdellitoral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.ItemPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;

@WithMockUser(username = "admin", roles = "ADMIN")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class PedidoCancelacionTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	void cancelaPedidoRecibidoYReponeStock() throws Exception {
		ContextoPedido contexto = crearContexto();
		Producto producto = crearProducto(contexto.local(), "Muzzarella", 10);
		Pedido pedido = crearPedido(contexto, EstadoPedido.RECIBIDO, new ProductoCantidad(producto, 3));

		mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedido.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("CANCELADO"));

		assertEquals(10, stockDe(producto));
	}

	@Test
	void cancelaPedidoEnPreparacionYReponeCantidadesExactas() throws Exception {
		ContextoPedido contexto = crearContexto();
		Producto pizza = crearProducto(contexto.local(), "Muzzarella", 12);
		Producto bebida = crearProducto(contexto.local(), "Gaseosa", 8);
		Pedido pedido = crearPedido(contexto, EstadoPedido.EN_PREPARACION, new ProductoCantidad(pizza, 4),
				new ProductoCantidad(bebida, 2));

		mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedido.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("CANCELADO"));

		assertEquals(12, stockDe(pizza));
		assertEquals(8, stockDe(bebida));
	}

	@Test
	void noCancelaPedidoEnCaminoYNoModificaStock() throws Exception {
		verificarEstadoNoCancelable(EstadoPedido.EN_CAMINO);
	}

	@Test
	void noCancelaPedidoEntregadoYNoModificaStock() throws Exception {
		verificarEstadoNoCancelable(EstadoPedido.ENTREGADO);
	}

	@Test
	void noCancelaPedidoCanceladoYNoModificaStock() throws Exception {
		verificarEstadoNoCancelable(EstadoPedido.CANCELADO);
	}

	@Test
	void cancelarDosVecesReponeStockUnaSolaVez() throws Exception {
		ContextoPedido contexto = crearContexto();
		Producto producto = crearProducto(contexto.local(), "Muzzarella", 10);
		Pedido pedido = crearPedido(contexto, EstadoPedido.RECIBIDO, new ProductoCantidad(producto, 3));

		mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedido.getId()))
			.andExpect(status().isOk());
		assertEquals(10, stockDe(producto));

		mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedido.getId()))
			.andExpect(status().isConflict());
		assertEquals(10, stockDe(producto));
	}

	@Test
	void devuelveNotFoundParaPedidoInexistente() throws Exception {
		mockMvc.perform(patch("/api/pedidos/{id}/cancelar", 999999))
			.andExpect(status().isNotFound());
	}

	private void verificarEstadoNoCancelable(EstadoPedido estado) throws Exception {
		ContextoPedido contexto = crearContexto();
		Producto producto = crearProducto(contexto.local(), "Muzzarella", 10);
		Pedido pedido = crearPedido(contexto, estado, new ProductoCantidad(producto, 3));

		mockMvc.perform(patch("/api/pedidos/{id}/cancelar", pedido.getId()))
			.andExpect(status().isConflict());
		assertEquals(7, stockDe(producto));
	}

	private ContextoPedido crearContexto() {
		String sufijo = UUID.randomUUID().toString().replace("-", "");

		Local local = new Local();
		local.setNombre("Local " + sufijo.substring(0, 16));
		local.setActivo(true);
		localRepository.save(local);

		Cliente cliente = new Cliente();
		cliente.setNombre("Cliente de prueba");
		cliente.setTelefono(sufijo.substring(0, 12));
		clienteRepository.save(cliente);

		return new ContextoPedido(local, cliente, sufijo);
	}

	private Producto crearProducto(Local local, String nombre, int stock) {
		Categoria categoria = categoriaRepository.findByNombre("Pizzas").orElseThrow();
		Producto producto = new Producto();
		producto.setLocal(local);
		producto.setCategoria(categoria);
		producto.setNombre(nombre);
		producto.setPrecio(new BigDecimal("1000.00"));
		producto.setStock(stock);
		producto.setActivo(true);
		return productoRepository.save(producto);
	}

	private Pedido crearPedido(ContextoPedido contexto, EstadoPedido estado, ProductoCantidad... productos) {
		Pedido pedido = new Pedido();
		pedido.setCodigoSeguimiento(contexto.sufijo().substring(0, 8).toUpperCase());
		pedido.setCliente(contexto.cliente());
		pedido.setLocal(contexto.local());
		pedido.setDireccionEntrega("Calle de prueba 123");
		pedido.setEstado(estado);
		pedido.setSubtotal(new BigDecimal("1000.00"));
		pedido.setDescuento(BigDecimal.ZERO.setScale(2));
		pedido.setCostoEnvio(new BigDecimal("1500.00"));
		pedido.setTotal(new BigDecimal("2500.00"));

		for (ProductoCantidad productoCantidad : productos) {
			productoCantidad.producto().setStock(productoCantidad.producto().getStock() - productoCantidad.cantidad());

			ItemPedido item = new ItemPedido();
			item.setProducto(productoCantidad.producto());
			item.setCantidad(productoCantidad.cantidad());
			item.setPrecioUnitario(productoCantidad.producto().getPrecio());
			pedido.agregarItem(item);
		}

		return pedidoRepository.saveAndFlush(pedido);
	}

	private Integer stockDe(Producto producto) {
		return productoRepository.findById(producto.getId()).orElseThrow().getStock();
	}

	private record ContextoPedido(Local local, Cliente cliente, String sufijo) {
	}

	private record ProductoCantidad(Producto producto, int cantidad) {
	}
}
