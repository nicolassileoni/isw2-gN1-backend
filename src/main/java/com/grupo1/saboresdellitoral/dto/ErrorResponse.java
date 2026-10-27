package com.grupo1.saboresdellitoral.dto;

import java.util.List;

public record ErrorResponse(String mensaje, List<String> detalles) {
}
