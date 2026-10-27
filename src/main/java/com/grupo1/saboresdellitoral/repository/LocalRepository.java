package com.grupo1.saboresdellitoral.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.Local;

public interface LocalRepository extends JpaRepository<Local, Integer> {

	Optional<Local> findByNombre(String nombre);

	boolean existsByNombre(String nombre);

	boolean existsByNombreAndIdNot(String nombre, Integer id);

	List<Local> findAllByOrderByNombreAsc();

	List<Local> findByActivoTrueOrderByNombreAsc();
}
