package com.grupo1.saboresdellitoral;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
class RepartidorCalleTests {

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
	void listaActivosConCantidadYCodigosEnCamino() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");
		Repartidor carlos = buscarRepartidor("Carlos Benítez");
		Repartidor micaela = buscarRepartidor("Micaela Ortiz");
		Repartidor ivan = buscarRepartidor("Iván Paredes");

		guardarPedido("CAL00001", EstadoPedido.EN_CAMINO, local, cliente, producto, carlos);
		guardarPedido("CAL00002", EstadoPedido.EN_CAMINO, local, cliente, producto, carlos);
		guardarPedido("CAL00003", EstadoPedido.EN_CAMINO, local, cliente, producto, carlos);
		guardarPedido("ENT00001", EstadoPedido.ENTREGADO, local, cliente, producto, carlos);
		guardarPedido("CAN00001", EstadoPedido.CANCELADO, local, cliente, producto, carlos);
		guardarPedido("CAL00004", EstadoPedido.EN_CAMINO, local, cliente, producto, micaela);

		mockMvc.perform(get("/api/repartidores/en-calle"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].repartidorId").value(carlos.getId()))
				.andExpect(jsonPath("$[0].nombreCompleto").value("Carlos Benítez"))
				.andExpect(jsonPath("$[0].cantidadPedidos").value(3))
				.andExpect(jsonPath("$[0].codigosPedidos",
						containsInAnyOrder("CAL00001", "CAL00002", "CAL00003")))
				.andExpect(jsonPath("$[1].repartidorId").value(ivan.getId()))
				.andExpect(jsonPath("$[1].nombreCompleto").value("Iván Paredes"))
				.andExpect(jsonPath("$[1].cantidadPedidos").value(0))
				.andExpect(jsonPath("$[1].codigosPedidos").isEmpty())
				.andExpect(jsonPath("$[2].repartidorId").value(micaela.getId()))
				.andExpect(jsonPath("$[2].nombreCompleto").value("Micaela Ortiz"))
				.andExpect(jsonPath("$[2].cantidadPedidos").value(1))
				.andExpect(jsonPath("$[2].codigosPedidos[0]").value("CAL00004"));
	}

	@Test
	void excluyeRepartidoresInactivos() throws Exception {
		Local local = guardarLocal("Centro");
		Producto producto = guardarProducto(local, "Muzzarella", "2500.00", 10);
		Cliente cliente = guardarCliente("Ana Pérez", "3425000000");
		Repartidor inactivo = guardarRepartidor("Inactivo", false);

		guardarPedido("CAL00005", EstadoPedido.EN_CAMINO, local, cliente, producto, inactivo);

		mockMvc.perform(get("/api/repartidores/en-calle"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[*].nombreCompleto", not(hasItem("Inactivo"))));
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

	private Repartidor buscarRepartidor(String nombre) {
		return repartidorRepository.findAll().stream()
				.filter(repartidor -> repartidor.getNombreCompleto().equals(nombre))
				.findFirst()
				.orElseThrow();
	}

	private Repartidor guardarRepartidor(String nombre, boolean activo) {
		Repartidor repartidor = new Repartidor();
		repartidor.setNombreCompleto(nombre);
		repartidor.setActivo(activo);
		return repartidorRepository.saveAndFlush(repartidor);
	}

	private void guardarPedido(String codigo, EstadoPedido estado, Local local, Cliente cliente, Producto producto,
			Repartidor repartidor) {
		BigDecimal precio = new BigDecimal("2500.00");
		BigDecimal subtotal = precio.setScale(2, RoundingMode.HALF_UP);
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
		item.setCantidad(1);
		item.setPrecioUnitario(precio);
		pedido.agregarItem(item);

		pedidoRepository.saveAndFlush(pedido);
	}
}
