package com.grupo1.saboresdellitoral.dto;

import java.time.LocalDateTime;

import com.grupo1.saboresdellitoral.entity.Rol;

public record UsuarioResponse(Integer id, String username, String nombreCompleto, Rol rol, Boolean activo,
		LocalDateTime fechaAlta) {
}
