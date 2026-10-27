package com.grupo1.saboresdellitoral.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "local")
@Getter
@Setter
@NoArgsConstructor
public class Local {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, length = 80)
	private String nombre;

	@Column(length = 120)
	private String direccion;

	@Column(length = 15)
	private String telefono;

	@Column(nullable = false)
	private Boolean activo = true;
}
