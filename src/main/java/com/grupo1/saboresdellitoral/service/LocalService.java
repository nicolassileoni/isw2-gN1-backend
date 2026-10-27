package com.grupo1.saboresdellitoral.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo1.saboresdellitoral.dto.LocalAdminResponse;
import com.grupo1.saboresdellitoral.dto.LocalRequest;
import com.grupo1.saboresdellitoral.entity.Local;
import com.grupo1.saboresdellitoral.exception.RecursoNoEncontradoException;
import com.grupo1.saboresdellitoral.exception.ReglaDeNegocioException;
import com.grupo1.saboresdellitoral.exception.SolicitudInvalidaException;
import com.grupo1.saboresdellitoral.repository.LocalRepository;

@Service
public class LocalService {

	private static final int NOMBRE_MAX = 80;
	private static final int DIRECCION_MAX = 120;
	private static final int TELEFONO_MAX = 15;

	private final LocalRepository localRepository;

	public LocalService(LocalRepository localRepository) {
		this.localRepository = localRepository;
	}

	@Transactional(readOnly = true)
	public List<LocalAdminResponse> listarTodos() {
		return localRepository.findAllByOrderByNombreAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public LocalAdminResponse crear(LocalRequest request) {
		String nombre = normalizarNombre(request);
		String direccion = normalizarOpcional(request.direccion(), DIRECCION_MAX, "La dirección");
		String telefono = normalizarOpcional(request.telefono(), TELEFONO_MAX, "El teléfono");

		if (localRepository.existsByNombre(nombre)) {
			throw new ReglaDeNegocioException("Ya existe un local con ese nombre");
		}

		Local local = new Local();
		local.setNombre(nombre);
		local.setDireccion(direccion);
		local.setTelefono(telefono);
		local.setActivo(true);

		return toResponse(localRepository.save(local));
	}

	@Transactional
	public LocalAdminResponse actualizar(Integer id, LocalRequest request) {
		Local local = buscarLocal(id);
		String nombre = normalizarNombre(request);
		String direccion = normalizarOpcional(request.direccion(), DIRECCION_MAX, "La dirección");
		String telefono = normalizarOpcional(request.telefono(), TELEFONO_MAX, "El teléfono");

		if (localRepository.existsByNombreAndIdNot(nombre, id)) {
			throw new ReglaDeNegocioException("Ya existe un local con ese nombre");
		}

		local.setNombre(nombre);
		local.setDireccion(direccion);
		local.setTelefono(telefono);

		return toResponse(localRepository.save(local));
	}

	@Transactional
	public LocalAdminResponse cambiarActivo(Integer id, Boolean activo) {
		if (activo == null) {
			throw new SolicitudInvalidaException("El campo activo es obligatorio");
		}

		Local local = buscarLocal(id);
		local.setActivo(activo);

		return toResponse(localRepository.save(local));
	}

	private Local buscarLocal(Integer id) {
		return localRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado"));
	}

	private String normalizarNombre(LocalRequest request) {
		if (request == null || request.nombre() == null || request.nombre().isBlank()) {
			throw new SolicitudInvalidaException("El nombre es obligatorio");
		}

		String nombre = request.nombre().trim();
		if (nombre.length() > NOMBRE_MAX) {
			throw new SolicitudInvalidaException(
					"El nombre no puede superar los " + NOMBRE_MAX + " caracteres");
		}

		return nombre;
	}

	private String normalizarOpcional(String valor, int max, String campo) {
		if (valor == null) {
			return null;
		}

		String normalizado = valor.trim();
		if (normalizado.isEmpty()) {
			return null;
		}

		if (normalizado.length() > max) {
			throw new SolicitudInvalidaException(
					campo + " no puede superar los " + max + " caracteres");
		}

		return normalizado;
	}

	private LocalAdminResponse toResponse(Local local) {
		return new LocalAdminResponse(local.getId(), local.getNombre(), local.getDireccion(), local.getTelefono(),
				local.getActivo());
	}
}
