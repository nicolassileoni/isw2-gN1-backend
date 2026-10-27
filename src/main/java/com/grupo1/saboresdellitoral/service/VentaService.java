package com.grupo1.saboresdellitoral.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.VentaLocalResponse;
import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Pedido;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.PedidoRepository;

@Service
public class VentaService {

	private final PedidoRepository pedidoRepository;
	private final LocalRepository localRepository;

	public VentaService(PedidoRepository pedidoRepository, LocalRepository localRepository) {
		this.pedidoRepository = pedidoRepository;
		this.localRepository = localRepository;
	}

	@Transactional(readOnly = true)
	public List<VentaLocalResponse> listarPorLocal(String desdeTexto, String hastaTexto) {
		LocalDate desde = validarFecha(desdeTexto, "desde");
		LocalDate hasta = validarFecha(hastaTexto, "hasta");
		if (desde.isAfter(hasta)) {
			throw new SolicitudInvalidaException("La fecha desde no puede ser posterior a la fecha hasta");
		}

		LocalDateTime inicio = desde.atStartOfDay();
		LocalDateTime finExclusivo = hasta.plusDays(1).atStartOfDay();
		Map<Integer, List<Pedido>> pedidosPorLocal = pedidoRepository
				.findByEstadoAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(EstadoPedido.ENTREGADO, inicio, finExclusivo)
				.stream()
				.collect(Collectors.groupingBy(pedido -> pedido.getLocal().getId()));

		return localRepository.findAllByOrderByNombreAsc().stream()
				.map(local -> aResponse(local, pedidosPorLocal.getOrDefault(local.getId(), List.of())))
				.toList();
	}

	private LocalDate validarFecha(String fechaTexto, String nombre) {
		if (fechaTexto == null || fechaTexto.isBlank()) {
			throw new SolicitudInvalidaException("La fecha " + nombre + " es obligatoria");
		}

		try {
			return LocalDate.parse(fechaTexto);
		} catch (DateTimeParseException exception) {
			throw new SolicitudInvalidaException("La fecha " + nombre + " no es válida");
		}
	}

	private VentaLocalResponse aResponse(Local local, List<Pedido> pedidos) {
		BigDecimal montoVendido = pedidos.stream()
				.map(pedido -> pedido.getSubtotal().subtract(pedido.getDescuento()))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
		return new VentaLocalResponse(local.getId(), local.getNombre(), pedidos.size(), montoVendido);
	}
}
