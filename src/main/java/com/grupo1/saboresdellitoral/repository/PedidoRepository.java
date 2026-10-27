package com.grupo1.saboresdellitoral.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	Optional<Pedido> findByCodigoSeguimiento(String codigoSeguimiento);

	boolean existsByCodigoSeguimiento(String codigoSeguimiento);

	List<Pedido> findByEstadoOrderByFechaHoraAsc(EstadoPedido estado);

	List<Pedido> findByEstadoInOrderByFechaHoraAsc(Collection<EstadoPedido> estados);

	List<Pedido> findByRepartidorIdAndEstadoOrderByFechaHoraAsc(Integer repartidorId, EstadoPedido estado);

	long countByRepartidorIdAndEstado(Integer repartidorId, EstadoPedido estado);
}
