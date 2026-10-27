package com.grupo1.saboresdellitoral.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.ItemPedido;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Integer> {

	List<ItemPedido> findByPedidoId(Integer pedidoId);
}
