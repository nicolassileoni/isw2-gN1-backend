package com.grupo1.saboresdellitoral.dto;

import com.grupo1.saboresdellitoral.entity.Rol;

public record ActualizarUsuarioRequest(String nombreCompleto, Rol rol) {
}
