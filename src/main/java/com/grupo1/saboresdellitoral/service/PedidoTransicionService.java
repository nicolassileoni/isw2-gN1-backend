package com.grupo1.saboresdellitoral.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.ClienteResponse;
import com.grupo1.saboresdellitoral.dto.ItemDetalleResponse;
import com.grupo1.saboresdellitoral.dto.LocalResumenResponse;
import com.grupo1.saboresdellitoral.dto.PedidoDetalleResponse;
import com.grupo1.saboresdellitoral.dto.RepartidorResumenResponse;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.ItemPedido;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Repartidor;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.exception.ReglaDeNegocioException;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;

@Service
public class PedidoTransicionService {

	private static final int MAX_PEDIDOS_EN_CAMINO = 3;

	private final PedidoRepository pedidoRepository;
	private final RepartidorRepository repartidorRepository;

	public PedidoTransicionService(PedidoRepository pedidoRepository, RepartidorRepository repartidorRepository) {
		this.pedidoRepository = pedidoRepository;
		this.repartidorRepository = repartidorRepository;
	}

	@Transactional
	public PedidoDetalleResponse avanzar(Integer id, Integer repartidorId) {
		Pedido pedido = pedidoRepository.buscarPorIdParaActualizar(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));

		switch (pedido.getEstado()) {
			case RECIBIDO -> pedido.setEstado(EstadoPedido.EN_PREPARACION);
			case EN_PREPARACION -> pasarAEnCamino(pedido, repartidorId);
			case EN_CAMINO -> pedido.setEstado(EstadoPedido.ENTREGADO);
			case ENTREGADO, CANCELADO -> throw new ReglaDeNegocioException("El pedido no puede avanzar desde su estado actual");
		}

		return aDetalle(pedido);
	}

	private void pasarAEnCamino(Pedido pedido, Integer repartidorId) {
		if (repartidorId == null) {
			throw new SolicitudInvalidaException("El repartidor es obligatorio para enviar el pedido");
		}

		Repartidor repartidor = repartidorRepository.buscarPorIdParaActualizar(repartidorId)
				.orElseThrow(() -> new SolicitudInvalidaException("El repartidor indicado no existe"));

		if (!Boolean.TRUE.equals(repartidor.getActivo())) {
			throw new ReglaDeNegocioException("El repartidor está inactivo");
		}

		if (pedidoRepository.countByRepartidorIdAndEstado(repartidorId, EstadoPedido.EN_CAMINO) >= MAX_PEDIDOS_EN_CAMINO) {
			throw new ReglaDeNegocioException("El repartidor alcanzó el límite de pedidos en camino");
		}

		pedido.setRepartidor(repartidor);
		pedido.setEstado(EstadoPedido.EN_CAMINO);
	}

	private PedidoDetalleResponse aDetalle(Pedido pedido) {
		List<ItemDetalleResponse> items = pedido.getItems().stream().map(this::aItemDetalle).toList();
		RepartidorResumenResponse repartidor = pedido.getRepartidor() == null ? null
				: new RepartidorResumenResponse(pedido.getRepartidor().getId(), pedido.getRepartidor().getNombreCompleto());

		return new PedidoDetalleResponse(pedido.getId(), pedido.getCodigoSeguimiento(), pedido.getEstado(),
				new LocalResumenResponse(pedido.getLocal().getId(), pedido.getLocal().getNombre()),
				new ClienteResponse(pedido.getCliente().getNombre(), pedido.getCliente().getTelefono()),
				pedido.getDireccionEntrega(), repartidor, pedido.getFechaHora(), items, pedido.getSubtotal(),
				pedido.getDescuento(), pedido.getCostoEnvio(), pedido.getTotal());
	}

	private ItemDetalleResponse aItemDetalle(ItemPedido item) {
		BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
				.setScale(2, RoundingMode.HALF_UP);
		return new ItemDetalleResponse(item.getProducto().getId(), item.getProducto().getNombre(), item.getCantidad(),
				item.getPrecioUnitario(), subtotal);
	}
}
