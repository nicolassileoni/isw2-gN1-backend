package com.grupo1.saboresdellitoral.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.RepartidorEnCalleResponse;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Repartidor;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;

@Service
public class RepartidorService {

	private final RepartidorRepository repartidorRepository;
	private final PedidoRepository pedidoRepository;

	public RepartidorService(RepartidorRepository repartidorRepository, PedidoRepository pedidoRepository) {
		this.repartidorRepository = repartidorRepository;
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional(readOnly = true)
	public List<RepartidorEnCalleResponse> listarEnCalle() {
		return repartidorRepository.findByActivoTrueOrderByNombreCompletoAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	private RepartidorEnCalleResponse aResponse(Repartidor repartidor) {
		List<Pedido> pedidos = pedidoRepository.findByRepartidorIdAndEstadoOrderByFechaHoraAsc(repartidor.getId(),
				EstadoPedido.EN_CAMINO);
		List<String> codigos = pedidos.stream().map(Pedido::getCodigoSeguimiento).toList();

		return new RepartidorEnCalleResponse(repartidor.getId(), repartidor.getNombreCompleto(), pedidos.size(),
				codigos);
	}
}
