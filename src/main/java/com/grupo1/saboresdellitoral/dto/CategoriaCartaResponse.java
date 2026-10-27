package com.grupo1.saboresdellitoral.dto;

import java.util.List;

public record CategoriaCartaResponse(Integer categoriaId, String categoriaNombre,
		List<ProductoCartaResponse> productos) {
}
