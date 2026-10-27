package com.grupo1.saboresdellitoral.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.exception.ReglaDeNegocioException;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;

@Service
public class PedidoCancelacionService {

	private final PedidoRepository pedidoRepository;
	private final ProductoRepository productoRepository;

	public PedidoCancelacionService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
		this.pedidoRepository = pedidoRepository;
		this.productoRepository = productoRepository;
	}

	@Transactional
	public PedidoDetalleResponse cancelar(Integer id) {
		Pedido pedido = pedidoRepository.buscarPorIdParaActualizar(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));

		if (pedido.getEstado() != EstadoPedido.RECIBIDO && pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
			throw new ReglaDeNegocioException("El pedido no puede cancelarse desde su estado actual");
		}

		reponerStock(pedido.getItems());
		pedido.setEstado(EstadoPedido.CANCELADO);

		return aDetalle(pedido);
	}

	private void reponerStock(List<ItemPedido> items) {
		List<Integer> productoIds = items.stream().map(item -> item.getProducto().getId()).sorted().toList();
		Map<Integer, Producto> productos = productoRepository.buscarPorIdsParaActualizar(productoIds).stream()
				.collect(Collectors.toMap(Producto::getId, Function.identity()));

		for (ItemPedido item : items) {
			Producto producto = productos.get(item.getProducto().getId());
			producto.setStock(producto.getStock() + item.getCantidad());
		}
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
