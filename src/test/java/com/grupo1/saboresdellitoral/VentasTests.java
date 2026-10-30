package com.grupo1.saboresdellitoral;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;

@WithMockUser(username = "admin", roles = "ADMIN")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class VentasTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void soloLosPedidosEntregadosCuentanComoVentas() throws Exception {
		Local local = crearLocal("Centro");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 10, 12, 0), "1000.00", "100.00");
		crearPedido(local, EstadoPedido.RECIBIDO, LocalDateTime.of(2026, 5, 10, 12, 0), "1000.00", "100.00");
		crearPedido(local, EstadoPedido.EN_PREPARACION, LocalDateTime.of(2026, 5, 10, 12, 0), "1000.00", "100.00");
		crearPedido(local, EstadoPedido.EN_CAMINO, LocalDateTime.of(2026, 5, 10, 12, 0), "1000.00", "100.00");
		crearPedido(local, EstadoPedido.CANCELADO, LocalDateTime.of(2026, 5, 10, 12, 0), "1000.00", "100.00");

		mockMvc.perform(get("/api/ventas").param("desde", "2026-05-10").param("hasta", "2026-05-10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].cantidadPedidos").value(1))
			.andExpect(jsonPath("$[0].montoVendido").value(900.00));
	}

	@Test
	void incluyeLosLimitesDelRangoYExcluyeElLimiteSuperior() throws Exception {
		Local local = crearLocal("Centro");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 10, 0, 0), "1000.00", "0.00");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 11, 23, 59, 59), "1000.00", "0.00");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 12, 0, 0), "1000.00", "0.00");

		mockMvc.perform(get("/api/ventas").param("desde", "2026-05-10").param("hasta", "2026-05-11"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].cantidadPedidos").value(2))
			.andExpect(jsonPath("$[0].montoVendido").value(2000.00));
	}

	@Test
	void noIncluyePedidosFueraDelRango() throws Exception {
		Local local = crearLocal("Centro");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 9, 23, 59), "1000.00", "0.00");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 12, 0, 0), "1000.00", "0.00");

		mockMvc.perform(get("/api/ventas").param("desde", "2026-05-10").param("hasta", "2026-05-11"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].cantidadPedidos").value(0))
			.andExpect(jsonPath("$[0].montoVendido").value(0.00));
	}

	@Test
	void montoVendidoExcluyeEnvioYRestaDescuento() throws Exception {
		Local local = crearLocal("Centro");
		crearPedido(local, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 10, 12, 0), "25000.00", "2500.00");

		mockMvc.perform(get("/api/ventas").param("desde", "2026-05-10").param("hasta", "2026-05-10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].montoVendido").value(22500.00));
	}

	@Test
	void incluyeLocalesSinVentasOrdenadosPorNombre() throws Exception {
		Local centro = crearLocal("Centro");
		crearLocal("Sin Ventas");
		crearPedido(centro, EstadoPedido.ENTREGADO, LocalDateTime.of(2026, 5, 10, 12, 0), "1000.00", "0.00");

		mockMvc.perform(get("/api/ventas").param("desde", "2026-05-10").param("hasta", "2026-05-10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].localNombre").value("Centro"))
			.andExpect(jsonPath("$[1].localNombre").value("Sin Ventas"))
			.andExpect(jsonPath("$[1].cantidadPedidos").value(0))
			.andExpect(jsonPath("$[1].montoVendido").value(0.00));
	}

	@Test
	void rechazaRangoInvalido() throws Exception {
		mockMvc.perform(get("/api/ventas").param("desde", "2026-05-12").param("hasta", "2026-05-10"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaParametrosFaltantesOFechaInvalida() throws Exception {
		mockMvc.perform(get("/api/ventas").param("hasta", "2026-05-10"))
			.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/ventas").param("desde", "fecha").param("hasta", "2026-05-10"))
			.andExpect(status().isBadRequest());
	}

	private Local crearLocal(String nombre) {
		Local local = new Local();
		local.setNombre(nombre);
		local.setActivo(true);
		return localRepository.save(local);
	}

	private void crearPedido(Local local, EstadoPedido estado, LocalDateTime fecha, String subtotal, String descuento) {
		String sufijo = UUID.randomUUID().toString().replace("-", "");
		Cliente cliente = new Cliente();
		cliente.setNombre("Cliente de prueba");
		cliente.setTelefono(sufijo.substring(0, 12));
		clienteRepository.save(cliente);

		BigDecimal subtotalMonto = new BigDecimal(subtotal);
		BigDecimal descuentoMonto = new BigDecimal(descuento);
		Pedido pedido = new Pedido();
		pedido.setCodigoSeguimiento(sufijo.substring(0, 8).toUpperCase());
		pedido.setCliente(cliente);
		pedido.setLocal(local);
		pedido.setDireccionEntrega("Calle de prueba 123");
		pedido.setEstado(estado);
		pedido.setSubtotal(subtotalMonto);
		pedido.setDescuento(descuentoMonto);
		pedido.setCostoEnvio(new BigDecimal("1500.00"));
		pedido.setTotal(subtotalMonto.subtract(descuentoMonto).add(new BigDecimal("1500.00")));
		Pedido guardado = pedidoRepository.saveAndFlush(pedido);

		jdbcTemplate.update("update pedido set fecha_hora = ? where id = ?", Timestamp.valueOf(fecha), guardado.getId());
	}
}
