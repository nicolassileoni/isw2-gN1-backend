package com.grupo1.saboresdellitoral.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	Optional<Categoria> findByNombre(String nombre);

	boolean existsByNombre(String nombre);

	List<Categoria> findAllByOrderByNombreAsc();
}
