package com.grupo1.saboresdellitoral;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.ItemPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.entity.Repartidor;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoConsultaTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private RepartidorRepository repartidorRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Test
	void seguimientoDevuelveDatosPublicosSinDatosPersonales() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();
		guardarPedido("ABC12345", EstadoPedido.EN_CAMINO, local, cliente, producto, repartidor, 2, "2500.00");

		mockMvc.perform(get("/api/pedidos/seguimiento/abc12345"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.codigoSeguimiento").value("ABC12345"))
				.andExpect(jsonPath("$.estado").value("EN_CAMINO"))
				.andExpect(jsonPath("$.localNombre").value("Centro"))
				.andExpect(jsonPath("$.items[0].productoNombre").value("Muzzarella"))
				.andExpect(jsonPath("$.items[0].cantidad").value(2))
				.andExpect(jsonPath("$.items[0].precioUnitario").value(2500.00))
				.andExpect(jsonPath("$.total").value(6500.00))
				.andExpect(jsonPath("$.fechaHora").exists())
				.andExpect(jsonPath("$.id").doesNotExist())
				.andExpect(jsonPath("$.cliente").doesNotExist())
				.andExpect(jsonPath("$.telefono").doesNotExist())
				.andExpect(jsonPath("$.direccionEntrega").doesNotExist())
				.andExpect(jsonPath("$.repartidor").doesNotExist());
	}

	@Test
	void seguimientoInexistenteDevuelveNotFound() throws Exception {
		mockMvc.perform(get("/api/pedidos/seguimiento/NOEXISTE"))
				.andExpect(status().isNotFound());
	}

	@Test
	void listadoPorDefectoMuestraPedidosNoTerminados() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");

		guardarPedido("REC00001", EstadoPedido.RECIBIDO, local, cliente, producto, null, 2, "2500.00");
		guardarPedido("PRE00001", EstadoPedido.EN_PREPARACION, local, cliente, producto, null, 2, "2500.00");
		guardarPedido("CAM00001", EstadoPedido.EN_CAMINO, local, cliente, producto, null, 2, "2500.00");
		guardarPedido("ENT00001", EstadoPedido.ENTREGADO, local, cliente, producto, null, 2, "2500.00");
		guardarPedido("CAN00001", EstadoPedido.CANCELADO, local, cliente, producto, null, 2, "2500.00");

		mockMvc.perform(get("/api/pedidos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[*].codigoSeguimiento",
						containsInAnyOrder("REC00001", "PRE00001", "CAM00001")));
	}

	@Test
	void listadoFiltraPorEstado() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");

		guardarPedido("REC00001", EstadoPedido.RECIBIDO, local, cliente, producto, null, 2, "2500.00");
		guardarPedido("CAM00001", EstadoPedido.EN_CAMINO, local, cliente, producto, null, 2, "2500.00");

		mockMvc.perform(get("/api/pedidos").param("estado", "EN_CAMINO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].codigoSeguimiento").value("CAM00001"))
				.andExpect(jsonPath("$[0].clienteNombre").value("Ana Pérez"))
				.andExpect(jsonPath("$[0].localNombre").value("Centro"))
				.andExpect(jsonPath("$[0].total").value(6500.00));
	}

	@Test
	void listadoRechazaEstadoInvalido() throws Exception {
		mockMvc.perform(get("/api/pedidos").param("estado", "VOLANDO"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void detalleMuestraDatosCompletos() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();
		Pedido pedido = guardarPedido("DET00001", EstadoPedido.EN_CAMINO, local, cliente, producto, repartidor, 2,
				"2500.00");

		mockMvc.perform(get("/api/pedidos/" + pedido.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(pedido.getId()))
				.andExpect(jsonPath("$.codigoSeguimiento").value("DET00001"))
				.andExpect(jsonPath("$.estado").value("EN_CAMINO"))
				.andExpect(jsonPath("$.local.id").value(local.getId()))
				.andExpect(jsonPath("$.local.nombre").value("Centro"))
				.andExpect(jsonPath("$.cliente.nombre").value("Ana Pérez"))
				.andExpect(jsonPath("$.cliente.telefono").value("3425000000"))
				.andExpect(jsonPath("$.direccionEntrega").value("San Martín 123"))
				.andExpect(jsonPath("$.repartidor.id").value(repartidor.getId()))
				.andExpect(jsonPath("$.repartidor.nombreCompleto").value(repartidor.getNombreCompleto()))
				.andExpect(jsonPath("$.items[0].productoId").value(producto.getId()))
				.andExpect(jsonPath("$.items[0].productoNombre").value("Muzzarella"))
				.andExpect(jsonPath("$.items[0].cantidad").value(2))
				.andExpect(jsonPath("$.items[0].precioUnitario").value(2500.00))
				.andExpect(jsonPath("$.items[0].subtotal").value(5000.00))
				.andExpect(jsonPath("$.subtotal").value(5000.00))
				.andExpect(jsonPath("$.descuento").value(0.00))
				.andExpect(jsonPath("$.costoEnvio").value(1500.00))
				.andExpect(jsonPath("$.total").value(6500.00))
				.andExpect(jsonPath("$.fechaHora").exists());
	}

	@Test
	void detalleSinRepartidorDevuelveRepartidorNulo() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");
		Pedido pedido = guardarPedido("DET00002", EstadoPedido.RECIBIDO, local, cliente, producto, null, 1, "2500.00");

		mockMvc.perform(get("/api/pedidos/" + pedido.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.repartidor").value(nullValue()));
	}

	@Test
	void detalleInexistenteDevuelveNotFound() throws Exception {
		mockMvc.perform(get("/api/pedidos/999999"))
				.andExpect(status().isNotFound());
	}

	private Local guardarLocal(String nombre) {
		Local local = new Local();
		local.setNombre(nombre);
		local.setActivo(true);
		return localRepository.saveAndFlush(local);
	}

	private Producto guardarProducto(Local local, String nombre, String precio, int stock) {
		Categoria categoria = categoriaRepository.findByNombre("Pizzas").orElseThrow();
		Producto producto = new Producto();
		producto.setLocal(local);
		producto.setCategoria(categoria);
		producto.setNombre(nombre);
		producto.setPrecio(new BigDecimal(precio));
		producto.setStock(stock);
		producto.setActivo(true);
		return productoRepository.saveAndFlush(producto);
	}

	private Cliente guardarCliente(String nombre, String telefono) {
		Cliente cliente = new Cliente();
		cliente.setNombre(nombre);
		cliente.setTelefono(telefono);
		return clienteRepository.saveAndFlush(cliente);
	}

	private Pedido guardarPedido(String codigo, EstadoPedido estado, Local local, Cliente cliente, Producto producto,
			Repartidor repartidor, int cantidad, String precioUnitario) {
		BigDecimal precio = new BigDecimal(precioUnitario);
		BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
		BigDecimal costoEnvio = new BigDecimal("1500.00");

		Pedido pedido = new Pedido();
		pedido.setCodigoSeguimiento(codigo);
		pedido.setCliente(cliente);
		pedido.setLocal(local);
		pedido.setRepartidor(repartidor);
		pedido.setDireccionEntrega("San Martín 123");
		pedido.setEstado(estado);
		pedido.setSubtotal(subtotal);
		pedido.setDescuento(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
		pedido.setCostoEnvio(costoEnvio);
		pedido.setTotal(subtotal.add(costoEnvio).setScale(2, RoundingMode.HALF_UP));

		ItemPedido item = new ItemPedido();
		item.setProducto(producto);
		item.setCantidad(cantidad);
		item.setPrecioUnitario(precio);
		pedido.agregarItem(item);

		return pedidoRepository.saveAndFlush(pedido);
	}
}
