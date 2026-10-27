package com.grupo1.saboresdellitoral.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.saboresdellitoral.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Optional<Usuario> findByUsername(String username);

	boolean existsByUsername(String username);

	List<Usuario> findAllByOrderByUsernameAsc();
}
