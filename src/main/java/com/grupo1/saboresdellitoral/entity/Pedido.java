package com.grupo1.saboresdellitoral.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 8)
	private String codigoSeguimiento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "local_id", nullable = false)
	private Local local;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "repartidor_id")
	private Repartidor repartidor;

	@Column(name = "direccion_entrega", nullable = false, length = 150)
	private String direccionEntrega;

	@CreationTimestamp
	@Column(name = "fecha_hora", nullable = false, updatable = false)
	private LocalDateTime fechaHora;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoPedido estado = EstadoPedido.RECIBIDO;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal subtotal;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal descuento;

	@Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
	private BigDecimal costoEnvio;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal total;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
	private List<ItemPedido> items = new ArrayList<>();

	public void agregarItem(ItemPedido item) {
		items.add(item);
		item.setPedido(this);
	}
}
