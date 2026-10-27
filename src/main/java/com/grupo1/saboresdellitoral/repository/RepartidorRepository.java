package com.grupo1.saboresdellitoral.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.Repartidor;

public interface RepartidorRepository extends JpaRepository<Repartidor, Integer> {

	List<Repartidor> findByActivoTrueOrderByNombreCompletoAsc();
}
