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
@Table(name = "repartidor")
@Getter
@Setter
@NoArgsConstructor
public class Repartidor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombreCompleto;

	@Column(length = 15)
	private String telefono;

	@Column(nullable = false)
	private Boolean activo = true;
}
