package com.grupo1.saboresdellitoral.dto;

import com.grupo1.saboresdellitoral.entity.Rol;

public record LoginResponse(String token, String nombreCompleto, Rol rol) {
}
