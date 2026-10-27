package com.grupo1.saboresdellitoral.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	Optional<Cliente> findByTelefono(String telefono);

	boolean existsByTelefono(String telefono);
}
