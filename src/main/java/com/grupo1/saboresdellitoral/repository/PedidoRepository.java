package com.grupo1.saboresdellitoral.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo1.saboresdellitoral.entity.EstadoPedido;
import com.grupo1.saboresdellitoral.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	@EntityGraph(attributePaths = { "local", "items", "items.producto" })
	Optional<Pedido> findByCodigoSeguimiento(String codigoSeguimiento);

	boolean existsByCodigoSeguimiento(String codigoSeguimiento);

	@EntityGraph(attributePaths = { "local", "cliente" })
	List<Pedido> findByEstadoOrderByFechaHoraAsc(EstadoPedido estado);

	@EntityGraph(attributePaths = { "local", "cliente" })
	List<Pedido> findByEstadoInOrderByFechaHoraAsc(Collection<EstadoPedido> estados);

	@EntityGraph(attributePaths = { "local", "cliente", "repartidor", "items", "items.producto" })
	@Query("select p from Pedido p where p.id = :id")
	Optional<Pedido> buscarDetallePorId(@Param("id") Integer id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Pedido p where p.id = :id")
	Optional<Pedido> buscarPorIdParaActualizar(@Param("id") Integer id);

	List<Pedido> findByRepartidorIdAndEstadoOrderByFechaHoraAsc(Integer repartidorId, EstadoPedido estado);

	long countByRepartidorIdAndEstado(Integer repartidorId, EstadoPedido estado);

	List<Pedido> findByEstadoAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(EstadoPedido estado,
			LocalDateTime desde, LocalDateTime hasta);
}
