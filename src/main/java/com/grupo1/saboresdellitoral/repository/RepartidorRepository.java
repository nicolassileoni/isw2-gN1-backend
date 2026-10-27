package com.grupo1.saboresdellitoral.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo1.saboresdellitoral.entity.Repartidor;

public interface RepartidorRepository extends JpaRepository<Repartidor, Integer> {

	List<Repartidor> findByActivoTrueOrderByNombreCompletoAsc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Repartidor r where r.id = :id")
	Optional<Repartidor> buscarPorIdParaActualizar(@Param("id") Integer id);
}
