package com.grupo1.saboresdellitoral.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	List<Producto> findByLocalIdAndActivoTrueOrderByNombreAsc(Integer localId);

	List<Producto> findByLocalIdOrderByNombreAsc(Integer localId);
}
