package com.grupo1.saboresdellitoral.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.ProductoAdminResponse;
import com.grupo1.saboresdellitoral.dto.ProductoRequest;
import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;

@Service
public class ProductoService {

	private static final int NOMBRE_MAX = 80;
	private static final int DESCRIPCION_MAX = 200;

	private final ProductoRepository productoRepository;
	private final LocalRepository localRepository;
	private final CategoriaRepository categoriaRepository;

	public ProductoService(ProductoRepository productoRepository, LocalRepository localRepository,
			CategoriaRepository categoriaRepository) {
		this.productoRepository = productoRepository;
		this.localRepository = localRepository;
		this.categoriaRepository = categoriaRepository;
	}

	@Transactional(readOnly = true)
	public List<ProductoAdminResponse> listar(Integer localId) {
		List<Producto> productos = localId == null ? productoRepository.findAll() : productoRepository.findByLocalIdOrderByNombreAsc(localId);
		return productos.stream().map(this::toResponse).toList();
	}

	@Transactional
	public ProductoAdminResponse crear(ProductoRequest request) {
		DatosProducto datos = validarDatos(request);

		Producto producto = new Producto();
		asignarDatos(producto, datos);
		producto.setActivo(true);

		return toResponse(productoRepository.save(producto));
	}

	@Transactional
	public ProductoAdminResponse actualizar(Integer id, ProductoRequest request) {
		Producto producto = buscarProducto(id);
		asignarDatos(producto, validarDatos(request));

		return toResponse(productoRepository.save(producto));
	}

	@Transactional
	public ProductoAdminResponse cambiarActivo(Integer id, Boolean activo) {
		if (activo == null) {
			throw new SolicitudInvalidaException("El campo activo es obligatorio");
		}

		Producto producto = buscarProducto(id);
		producto.setActivo(activo);

		return toResponse(productoRepository.save(producto));
	}

	private DatosProducto validarDatos(ProductoRequest request) {
		if (request == null) {
			throw new SolicitudInvalidaException("Los datos del producto son obligatorios");
		}

		Local local = buscarLocal(request.localId());
		Categoria categoria = buscarCategoria(request.categoriaId());
		String nombre = normalizarObligatorio(request.nombre(), NOMBRE_MAX, "El nombre");
		String descripcion = normalizarOpcional(request.descripcion(), DESCRIPCION_MAX, "La descripción");
		BigDecimal precio = normalizarPrecio(request.precio());
		Integer stock = validarStock(request.stock());

		return new DatosProducto(local, categoria, nombre, descripcion, precio, stock);
	}

	private Local buscarLocal(Integer localId) {
		if (localId == null) {
			throw new SolicitudInvalidaException("El local es obligatorio");
		}

		return localRepository.findById(localId)
				.orElseThrow(() -> new SolicitudInvalidaException("El local indicado no existe"));
	}

	private Categoria buscarCategoria(Integer categoriaId) {
		if (categoriaId == null) {
			throw new SolicitudInvalidaException("La categoría es obligatoria");
		}

		return categoriaRepository.findById(categoriaId)
				.orElseThrow(() -> new SolicitudInvalidaException("La categoría indicada no existe"));
	}

	private Producto buscarProducto(Integer id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
	}

	private String normalizarObligatorio(String valor, int maximo, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new SolicitudInvalidaException(campo + " es obligatorio");
		}

		String normalizado = valor.trim();
		if (normalizado.length() > maximo) {
			throw new SolicitudInvalidaException(campo + " no puede superar los " + maximo + " caracteres");
		}

		return normalizado;
	}

	private String normalizarOpcional(String valor, int maximo, String campo) {
		if (valor == null) {
			return null;
		}

		String normalizado = valor.trim();
		if (normalizado.isEmpty()) {
			return null;
		}

		if (normalizado.length() > maximo) {
			throw new SolicitudInvalidaException(campo + " no puede superar los " + maximo + " caracteres");
		}

		return normalizado;
	}

	private BigDecimal normalizarPrecio(BigDecimal precio) {
		if (precio == null) {
			throw new SolicitudInvalidaException("El precio es obligatorio");
		}

		if (precio.signum() < 0) {
			throw new SolicitudInvalidaException("El precio no puede ser negativo");
		}

		return precio.setScale(2, RoundingMode.HALF_UP);
	}

	private Integer validarStock(Integer stock) {
		if (stock == null) {
			throw new SolicitudInvalidaException("El stock es obligatorio");
		}

		if (stock < 0) {
			throw new SolicitudInvalidaException("El stock no puede ser negativo");
		}

		return stock;
	}

	private void asignarDatos(Producto producto, DatosProducto datos) {
		producto.setLocal(datos.local());
		producto.setCategoria(datos.categoria());
		producto.setNombre(datos.nombre());
		producto.setDescripcion(datos.descripcion());
		producto.setPrecio(datos.precio());
		producto.setStock(datos.stock());
	}

	private ProductoAdminResponse toResponse(Producto producto) {
		return new ProductoAdminResponse(producto.getId(), producto.getLocal().getId(), producto.getLocal().getNombre(),
				producto.getCategoria().getId(), producto.getCategoria().getNombre(), producto.getNombre(),
				producto.getDescripcion(), producto.getPrecio().setScale(2, RoundingMode.HALF_UP), producto.getStock(),
				producto.getActivo());
	}

	private record DatosProducto(Local local, Categoria categoria, String nombre, String descripcion, BigDecimal precio,
			Integer stock) {
	}
}
