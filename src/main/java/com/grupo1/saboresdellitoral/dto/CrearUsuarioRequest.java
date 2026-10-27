package com.grupo1.saboresdellitoral.dto;

import com.grupo1.saboresdellitoral.entity.Rol;

public record CrearUsuarioRequest(String username, String password, String nombreCompleto, Rol rol) {
}
