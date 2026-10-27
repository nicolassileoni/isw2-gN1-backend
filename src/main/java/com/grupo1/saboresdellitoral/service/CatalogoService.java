package com.grupo1.saboresdellitoral.service;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.CartaResponse;
import com.grupo1.saboresdellitoral.dto.CategoriaCartaResponse;
import com.grupo1.saboresdellitoral.dto.CategoriaResponse;
import com.grupo1.saboresdellitoral.dto.LocalResponse;
import com.grupo1.saboresdellitoral.dto.ProductoCartaResponse;
import com.grupo1.saboresdellitoral.dto.RepartidorResponse;
import com.grupo1.saboresdellitoral.entity.Categoria;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.entity.Producto;
import com.grupo1.saboresdellitoral.entity.Repartidor;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.repository.CategoriaRepository;
import com.grupo1.saboresdellitoral.repository.LocalRepository;
import com.grupo1.saboresdellitoral.repository.ProductoRepository;
import com.grupo1.saboresdellitoral.repository.RepartidorRepository;

@Service
public class CatalogoService {

	private final CategoriaRepository categoriaRepository;
	private final LocalRepository localRepository;
	private final ProductoRepository productoRepository;
	private final RepartidorRepository repartidorRepository;

	public CatalogoService(CategoriaRepository categoriaRepository, LocalRepository localRepository,
			ProductoRepository productoRepository, RepartidorRepository repartidorRepository) {
		this.categoriaRepository = categoriaRepository;
		this.localRepository = localRepository;
		this.productoRepository = productoRepository;
		this.repartidorRepository = repartidorRepository;
	}

	@Transactional(readOnly = true)
	public List<CategoriaResponse> listarCategorias() {
		return categoriaRepository.findAllByOrderByNombreAsc().stream().map(this::aCategoriaResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<LocalResponse> listarLocales() {
		return localRepository.findByActivoTrueOrderByNombreAsc().stream().map(this::aLocalResponse).toList();
	}

	@Transactional(readOnly = true)
	public CartaResponse obtenerCarta(Integer localId) {
		Local local = localRepository.findById(localId)
				.filter(Local::getActivo)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el local solicitado."));

		List<Producto> productos = productoRepository.findByLocalIdAndActivoTrueOrderByNombreAsc(localId);

		Map<Integer, List<Producto>> productosPorCategoria = productos.stream()
				.collect(Collectors.groupingBy(producto -> producto.getCategoria().getId(), LinkedHashMap::new,
						Collectors.toList()));

		List<CategoriaCartaResponse> categorias = productosPorCategoria.values().stream()
				.map(this::aCategoriaCartaResponse)
				.sorted(Comparator.comparing(CategoriaCartaResponse::categoriaNombre))
				.toList();

		return new CartaResponse(local.getId(), local.getNombre(), categorias);
	}

	@Transactional(readOnly = true)
	public List<RepartidorResponse> listarRepartidores() {
		return repartidorRepository.findByActivoTrueOrderByNombreCompletoAsc().stream().map(this::aRepartidorResponse)
				.toList();
	}

	private CategoriaResponse aCategoriaResponse(Categoria categoria) {
		return new CategoriaResponse(categoria.getId(), categoria.getNombre());
	}

	private LocalResponse aLocalResponse(Local local) {
		return new LocalResponse(local.getId(), local.getNombre(), local.getDireccion(), local.getTelefono());
	}

	private CategoriaCartaResponse aCategoriaCartaResponse(List<Producto> productos) {
		Categoria categoria = productos.get(0).getCategoria();
		List<ProductoCartaResponse> productosCarta = productos.stream().map(this::aProductoCartaResponse).toList();
		return new CategoriaCartaResponse(categoria.getId(), categoria.getNombre(), productosCarta);
	}

	private ProductoCartaResponse aProductoCartaResponse(Producto producto) {
		return new ProductoCartaResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(),
				producto.getPrecio().setScale(2, RoundingMode.HALF_UP), producto.getStock(),
				producto.getStock() > 0);
	}

	private RepartidorResponse aRepartidorResponse(Repartidor repartidor) {
		return new RepartidorResponse(repartidor.getId(), repartidor.getNombreCompleto(), repartidor.getTelefono());
	}
}
