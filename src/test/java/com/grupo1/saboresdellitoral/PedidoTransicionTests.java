package com.grupo1.saboresdellitoral;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Repartidor;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoTransicionTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private RepartidorRepository repartidorRepository;

	@Autowired
	private LocalRepository localRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Test
	void avanzaLaCadenaCompletaDeEstados() throws Exception {
		Pedido pedido = crearPedido(EstadoPedido.RECIBIDO, null);
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();

		mockMvc.perform(avanzar(pedido.getId(), null))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("EN_PREPARACION"));

		mockMvc.perform(avanzar(pedido.getId(), repartidor.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("EN_CAMINO"))
			.andExpect(jsonPath("$.repartidor.id").value(repartidor.getId()));

		mockMvc.perform(avanzar(pedido.getId(), null))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("ENTREGADO"));
	}

	@Test
	void noAvanzaPedidoEntregado() throws Exception {
		Pedido pedido = crearPedido(EstadoPedido.ENTREGADO, repartidorRepository.findById(1).orElseThrow());

		mockMvc.perform(avanzar(pedido.getId(), null))
			.andExpect(status().isConflict());
	}

	@Test
	void noAvanzaPedidoCancelado() throws Exception {
		Pedido pedido = crearPedido(EstadoPedido.CANCELADO, null);

		mockMvc.perform(avanzar(pedido.getId(), null))
			.andExpect(status().isConflict());
	}

	@Test
	void requiereRepartidorParaPasarAEnCamino() throws Exception {
		Pedido pedido = crearPedido(EstadoPedido.EN_PREPARACION, null);

		mockMvc.perform(avanzar(pedido.getId(), null))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rechazaRepartidorInactivo() throws Exception {
		Pedido pedido = crearPedido(EstadoPedido.EN_PREPARACION, null);
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();
		repartidor.setActivo(false);

		mockMvc.perform(avanzar(pedido.getId(), repartidor.getId()))
			.andExpect(status().isConflict());
	}

	@Test
	void rechazaRepartidorInexistente() throws Exception {
		Pedido pedido = crearPedido(EstadoPedido.EN_PREPARACION, null);

		mockMvc.perform(avanzar(pedido.getId(), 999999))
			.andExpect(status().isBadRequest());
	}

	@Test
	void aceptaElTercerPedidoEnCaminoDelRepartidor() throws Exception {
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();
		crearPedido(EstadoPedido.EN_CAMINO, repartidor);
		crearPedido(EstadoPedido.EN_CAMINO, repartidor);
		Pedido pedido = crearPedido(EstadoPedido.EN_PREPARACION, null);

		mockMvc.perform(avanzar(pedido.getId(), repartidor.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("EN_CAMINO"));
	}

	@Test
	void rechazaRepartidorConTresPedidosEnCamino() throws Exception {
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();
		crearPedido(EstadoPedido.EN_CAMINO, repartidor);
		crearPedido(EstadoPedido.EN_CAMINO, repartidor);
		crearPedido(EstadoPedido.EN_CAMINO, repartidor);
		Pedido pedido = crearPedido(EstadoPedido.EN_PREPARACION, null);

		mockMvc.perform(avanzar(pedido.getId(), repartidor.getId()))
			.andExpect(status().isConflict());
	}

	@Test
	void conservaElRepartidorAlEntregar() throws Exception {
		Repartidor repartidor = repartidorRepository.findById(1).orElseThrow();
		Pedido pedido = crearPedido(EstadoPedido.EN_CAMINO, repartidor);

		mockMvc.perform(avanzar(pedido.getId(), null))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.estado").value("ENTREGADO"))
			.andExpect(jsonPath("$.repartidor.id").value(repartidor.getId()));
	}

	@Test
	void devuelveNotFoundParaPedidoInexistente() throws Exception {
		mockMvc.perform(avanzar(999999, null))
			.andExpect(status().isNotFound());
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder avanzar(Integer pedidoId,
			Integer repartidorId) {
		if (repartidorId == null) {
			return patch("/api/pedidos/{id}/avanzar", pedidoId).contentType(MediaType.APPLICATION_JSON);
		}
		return patch("/api/pedidos/{id}/avanzar", pedidoId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"repartidorId\":" + repartidorId + "}");
	}

	private Pedido crearPedido(EstadoPedido estado, Repartidor repartidor) {
		String sufijo = UUID.randomUUID().toString().replace("-", "");

		Local local = new Local();
		local.setNombre("Local " + sufijo.substring(0, 16));
		local.setActivo(true);
		localRepository.save(local);

		Cliente cliente = new Cliente();
		cliente.setNombre("Cliente de prueba");
		cliente.setTelefono(sufijo.substring(0, 12));
		clienteRepository.save(cliente);

		Pedido pedido = new Pedido();
		pedido.setCodigoSeguimiento(sufijo.substring(0, 8).toUpperCase());
		pedido.setCliente(cliente);
		pedido.setLocal(local);
		pedido.setRepartidor(repartidor);
		pedido.setDireccionEntrega("Calle de prueba 123");
		pedido.setEstado(estado);
		pedido.setSubtotal(new BigDecimal("1000.00"));
		pedido.setDescuento(BigDecimal.ZERO.setScale(2));
		pedido.setCostoEnvio(new BigDecimal("1500.00"));
		pedido.setTotal(new BigDecimal("2500.00"));
		return pedidoRepository.saveAndFlush(pedido);
	}
}
