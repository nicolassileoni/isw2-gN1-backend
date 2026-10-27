package com.grupo1.saboresdellitoral.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo1.saboresdellitoral.entity.Producto;

import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	List<Producto> findByLocalIdAndActivoTrueOrderByNombreAsc(Integer localId);

	List<Producto> findByLocalIdOrderByNombreAsc(Integer localId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Producto p where p.id in :ids order by p.id")
	List<Producto> buscarPorIdsParaActualizar(@Param("ids") Collection<Integer> ids);
}
