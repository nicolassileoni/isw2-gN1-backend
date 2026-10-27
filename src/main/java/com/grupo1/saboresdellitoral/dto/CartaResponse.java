package com.grupo1.saboresdellitoral.dto;

import java.util.List;

public record CartaResponse(Integer localId, String localNombre, List<CategoriaCartaResponse> categorias) {
}
