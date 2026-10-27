package com.grupo1.saboresdellitoral.dto;

import com.grupo1.saboresdellitoral.entity.Rol;

public record LoginResponse(Integer id, String username, String nombreCompleto, Rol rol) {
}
