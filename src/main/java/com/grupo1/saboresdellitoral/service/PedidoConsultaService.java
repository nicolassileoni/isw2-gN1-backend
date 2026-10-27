package com.grupo1.saboresdellitoral.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.ClienteResponse;
import com.grupo1.saboresdellitoral.dto.ItemDetalleResponse;
import com.grupo1.saboresdellitoral.dto.ItemSeguimientoResponse;
import com.grupo1.saboresdellitoral.dto.LocalResumenResponse;
import com.grupo1.saboresdellitoral.dto.PedidoDetalleResponse;
import com.grupo1.saboresdellitoral.dto.PedidoResumenResponse;
import com.grupo1.saboresdellitoral.dto.RepartidorResumenResponse;
import com.grupo1.saboresdellitoral.dto.SeguimientoResponse;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.ItemPedido;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;

@Service
public class PedidoConsultaService {

	private static final List<EstadoPedido> ESTADOS_NO_TERMINADOS = List.of(EstadoPedido.RECIBIDO,
			EstadoPedido.EN_PREPARACION, EstadoPedido.EN_CAMINO);

	private final PedidoRepository pedidoRepository;

	public PedidoConsultaService(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional(readOnly = true)
	public SeguimientoResponse obtenerSeguimiento(String codigo) {
		String codigoNormalizado = normalizarCodigo(codigo);
		Pedido pedido = pedidoRepository.findByCodigoSeguimiento(codigoNormalizado)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pedido solicitado."));

		return aSeguimiento(pedido);
	}

	@Transactional(readOnly = true)
	public List<PedidoResumenResponse> listar(EstadoPedido estado) {
		List<Pedido> pedidos = estado == null
				? pedidoRepository.findByEstadoInOrderByFechaHoraAsc(ESTADOS_NO_TERMINADOS)
				: pedidoRepository.findByEstadoOrderByFechaHoraAsc(estado);

		return pedidos.stream().map(this::aResumen).toList();
	}

	@Transactional(readOnly = true)
	public PedidoDetalleResponse obtenerDetalle(Integer id) {
		Pedido pedido = pedidoRepository.buscarDetallePorId(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pedido solicitado."));

		return aDetalle(pedido);
	}

	private String normalizarCodigo(String codigo) {
		if (codigo == null || codigo.isBlank()) {
			throw new RecursoNoEncontradoException("No se encontró el pedido solicitado.");
		}
		return codigo.trim().toUpperCase(Locale.ROOT);
	}

	private SeguimientoResponse aSeguimiento(Pedido pedido) {
		List<ItemSeguimientoResponse> items = pedido.getItems().stream()
				.map(item -> new ItemSeguimientoResponse(item.getProducto().getNombre(), item.getCantidad(),
						item.getPrecioUnitario()))
				.toList();

		return new SeguimientoResponse(pedido.getCodigoSeguimiento(), pedido.getEstado(),
				pedido.getLocal().getNombre(), items, pedido.getTotal(), pedido.getFechaHora());
	}

	private PedidoResumenResponse aResumen(Pedido pedido) {
		return new PedidoResumenResponse(pedido.getId(), pedido.getCodigoSeguimiento(), pedido.getEstado(),
				pedido.getLocal().getNombre(), pedido.getCliente().getNombre(), pedido.getFechaHora(),
				pedido.getTotal());
	}

	private PedidoDetalleResponse aDetalle(Pedido pedido) {
		List<ItemDetalleResponse> items = pedido.getItems().stream().map(this::aItemDetalle).toList();

		RepartidorResumenResponse repartidor = pedido.getRepartidor() == null ? null
				: new RepartidorResumenResponse(pedido.getRepartidor().getId(),
						pedido.getRepartidor().getNombreCompleto());

		return new PedidoDetalleResponse(pedido.getId(), pedido.getCodigoSeguimiento(), pedido.getEstado(),
				new LocalResumenResponse(pedido.getLocal().getId(), pedido.getLocal().getNombre()),
				new ClienteResponse(pedido.getCliente().getNombre(), pedido.getCliente().getTelefono()),
				pedido.getDireccionEntrega(), repartidor, pedido.getFechaHora(), items, pedido.getSubtotal(),
				pedido.getDescuento(), pedido.getCostoEnvio(), pedido.getTotal());
	}

	private ItemDetalleResponse aItemDetalle(ItemPedido item) {
		BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
				.setScale(2, RoundingMode.HALF_UP);

		return new ItemDetalleResponse(item.getProducto().getId(), item.getProducto().getNombre(),
				item.getCantidad(), item.getPrecioUnitario(), subtotal);
	}
}
