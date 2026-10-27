package com.grupo1.saboresdellitoral.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, length = 40)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 60)
	private String password;

	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombreCompleto;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Rol rol;

	@Column(nullable = false)
	private Boolean activo = true;

	@CreationTimestamp
	@Column(name = "fecha_alta", nullable = false, updatable = false)
	private LocalDateTime fechaAlta;
}
