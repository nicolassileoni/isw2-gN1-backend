package com.grupo1.saboresdellitoral.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.ClientePedidoRequest;
import com.grupo1.saboresdellitoral.dto.CrearPedidoRequest;
import com.grupo1.saboresdellitoral.dto.ItemPedidoRequest;
import com.grupo1.saboresdellitoral.dto.PedidoCreadoResponse;
import com.grupo1.saboresdellitoral.entity.Cliente;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.ItemPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.exception.ReglaDeNegocioException;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;
import com.grupo1.saboresdellitoral.repository.ClienteRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;

@Service
public class PedidoService {

	private static final BigDecimal LIMITE_DESCUENTO = new BigDecimal("20000.00");
	private static final BigDecimal TASA_DESCUENTO = new BigDecimal("0.10");
	private static final BigDecimal COSTO_ENVIO = new BigDecimal("1500.00");
	private static final int LONGITUD_CODIGO = 8;
	private static final int MAX_INTENTOS_CODIGO = 10;
	private static final String ALFABETO_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final LocalRepository localRepository;
	private final ProductoRepository productoRepository;
	private final ClienteRepository clienteRepository;
	private final PedidoRepository pedidoRepository;

	public PedidoService(LocalRepository localRepository, ProductoRepository productoRepository,
			ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
		this.localRepository = localRepository;
		this.productoRepository = productoRepository;
		this.clienteRepository = clienteRepository;
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional
	public PedidoCreadoResponse crear(CrearPedidoRequest request) {
		if (request == null) {
			throw new SolicitudInvalidaException("La solicitud es obligatoria");
		}

		Integer localId = validarLocalId(request.localId());
		String direccionEntrega = validarDireccionEntrega(request.direccionEntrega());
		ClientePedidoRequest clienteRequest = validarCliente(request.cliente());
		List<ItemPedidoRequest> items = validarItems(request.items());

		Local local = localRepository.findById(localId)
				.orElseThrow(() -> new SolicitudInvalidaException("El local indicado no existe"));
		if (!Boolean.TRUE.equals(local.getActivo())) {
			throw new ReglaDeNegocioException("El local está inactivo");
		}

		Map<Integer, Producto> productos = buscarProductos(items);
		validarProductos(items, productos, localId);

		Cliente cliente = obtenerOCrearCliente(clienteRequest.nombre(), clienteRequest.telefono());

		BigDecimal subtotal = BigDecimal.ZERO;
		List<ItemPedido> itemsPedido = new ArrayList<>();
		for (ItemPedidoRequest item : items) {
			Producto producto = productos.get(item.productoId());
			BigDecimal precioUnitario = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
			subtotal = subtotal.add(precioUnitario.multiply(BigDecimal.valueOf(item.cantidad())));

			ItemPedido itemPedido = new ItemPedido();
			itemPedido.setProducto(producto);
			itemPedido.setCantidad(item.cantidad());
			itemPedido.setPrecioUnitario(precioUnitario);
			itemsPedido.add(itemPedido);
		}

		subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
		BigDecimal descuento = calcularDescuento(subtotal);
		BigDecimal total = subtotal.subtract(descuento).add(COSTO_ENVIO).setScale(2, RoundingMode.HALF_UP);

		Pedido pedido = new Pedido();
		pedido.setCodigoSeguimiento(generarCodigoUnico());
		pedido.setCliente(cliente);
		pedido.setLocal(local);
		pedido.setDireccionEntrega(direccionEntrega);
		pedido.setEstado(EstadoPedido.RECIBIDO);
		pedido.setSubtotal(subtotal);
		pedido.setDescuento(descuento);
		pedido.setCostoEnvio(COSTO_ENVIO);
		pedido.setTotal(total);

		for (ItemPedido itemPedido : itemsPedido) {
			pedido.agregarItem(itemPedido);
		}
		for (ItemPedido itemPedido : itemsPedido) {
			Producto producto = itemPedido.getProducto();
			producto.setStock(producto.getStock() - itemPedido.getCantidad());
		}

		Pedido guardado = pedidoRepository.saveAndFlush(pedido);
		return aResponse(guardado);
	}

	private Integer validarLocalId(Integer localId) {
		if (localId == null) {
			throw new SolicitudInvalidaException("El local es obligatorio");
		}
		return localId;
	}

	private String validarDireccionEntrega(String direccionEntrega) {
		if (direccionEntrega == null || direccionEntrega.isBlank()) {
			throw new SolicitudInvalidaException("La dirección de entrega es obligatoria");
		}

		String direccion = direccionEntrega.trim();
		if (direccion.length() > 150) {
			throw new SolicitudInvalidaException("La dirección de entrega no puede superar los 150 caracteres");
		}
		return direccion;
	}

	private ClientePedidoRequest validarCliente(ClientePedidoRequest cliente) {
		if (cliente == null || cliente.nombre() == null || cliente.nombre().isBlank()) {
			throw new SolicitudInvalidaException("El nombre del cliente es obligatorio");
		}

		String nombre = cliente.nombre().trim();
		if (nombre.length() > 80) {
			throw new SolicitudInvalidaException("El nombre del cliente no puede superar los 80 caracteres");
		}

		if (cliente.telefono() == null || !cliente.telefono().matches("[0-9]{8,15}")) {
			throw new SolicitudInvalidaException("El teléfono debe contener entre 8 y 15 dígitos");
		}

		return new ClientePedidoRequest(nombre, cliente.telefono());
	}

	private List<ItemPedidoRequest> validarItems(List<ItemPedidoRequest> items) {
		if (items == null || items.isEmpty()) {
			throw new SolicitudInvalidaException("El pedido debe tener al menos un producto");
		}

		Set<Integer> productos = new HashSet<>();
		for (ItemPedidoRequest item : items) {
			if (item == null || item.productoId() == null) {
				throw new SolicitudInvalidaException("El producto es obligatorio");
			}
			if (item.cantidad() == null || item.cantidad() < 1) {
				throw new SolicitudInvalidaException("La cantidad debe ser mayor o igual a 1");
			}
			if (!productos.add(item.productoId())) {
				throw new SolicitudInvalidaException("El pedido no puede repetir productos");
			}
		}
		return items;
	}

	private Map<Integer, Producto> buscarProductos(List<ItemPedidoRequest> items) {
		List<Integer> ids = items.stream().map(ItemPedidoRequest::productoId).sorted().toList();
		List<Producto> productos = productoRepository.buscarPorIdsParaActualizar(ids);

		Map<Integer, Producto> productosPorId = productos.stream()
				.collect(Collectors.toMap(Producto::getId, Function.identity()));

		for (Integer id : ids) {
			if (!productosPorId.containsKey(id)) {
				throw new SolicitudInvalidaException("El producto indicado no existe");
			}
		}
		return productosPorId;
	}

	private void validarProductos(List<ItemPedidoRequest> items, Map<Integer, Producto> productos, Integer localId) {
		for (ItemPedidoRequest item : items) {
			Producto producto = productos.get(item.productoId());
			if (!producto.getLocal().getId().equals(localId)) {
				throw new ReglaDeNegocioException("El producto no pertenece al local");
			}
			if (!Boolean.TRUE.equals(producto.getActivo())) {
				throw new ReglaDeNegocioException("El producto está inactivo");
			}
			if (producto.getStock() < item.cantidad()) {
				throw new ReglaDeNegocioException("Stock insuficiente para el producto " + producto.getNombre());
			}
		}
	}

	private Cliente obtenerOCrearCliente(String nombre, String telefono) {
		return clienteRepository.findByTelefono(telefono).orElseGet(() -> {
			Cliente cliente = new Cliente();
			cliente.setNombre(nombre);
			cliente.setTelefono(telefono);
			try {
				return clienteRepository.saveAndFlush(cliente);
			} catch (DataIntegrityViolationException exception) {
				return clienteRepository.findByTelefono(telefono)
						.orElseThrow(() -> new ReglaDeNegocioException("No se pudo registrar el cliente"));
			}
		});
	}

	private BigDecimal calcularDescuento(BigDecimal subtotal) {
		if (subtotal.compareTo(LIMITE_DESCUENTO) > 0) {
			return subtotal.multiply(TASA_DESCUENTO).setScale(2, RoundingMode.HALF_UP);
		}
		return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	}

	private String generarCodigoUnico() {
		for (int intento = 0; intento < MAX_INTENTOS_CODIGO; intento++) {
			StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
			for (int i = 0; i < LONGITUD_CODIGO; i++) {
				codigo.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
			}
			if (!pedidoRepository.existsByCodigoSeguimiento(codigo.toString())) {
				return codigo.toString();
			}
		}
		throw new ReglaDeNegocioException("No se pudo generar un código de seguimiento único");
	}

	private PedidoCreadoResponse aResponse(Pedido pedido) {
		return new PedidoCreadoResponse(pedido.getCodigoSeguimiento(), pedido.getTotal(), pedido.getEstado(),
				pedido.getFechaHora());
	}
}
