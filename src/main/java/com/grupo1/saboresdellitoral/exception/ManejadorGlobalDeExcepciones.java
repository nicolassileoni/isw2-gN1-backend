package com.grupo1.saboresdellitoral.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.grupo1.saboresdellitoral.dto.ErrorResponse;

@RestControllerAdvice
public class ManejadorGlobalDeExcepciones {

	private static final Logger logger = LoggerFactory.getLogger(ManejadorGlobalDeExcepciones.class);

	@ExceptionHandler(SolicitudInvalidaException.class)
	public ResponseEntity<ErrorResponse> manejarSolicitudInvalida(SolicitudInvalidaException exception) {
		return respuesta(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(RecursoNoEncontradoException exception) {
		return respuesta(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(ReglaDeNegocioException.class)
	public ResponseEntity<ErrorResponse> manejarReglaDeNegocio(ReglaDeNegocioException exception) {
		return respuesta(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(NoAutorizadoException.class)
	public ResponseEntity<ErrorResponse> manejarNoAutorizado(NoAutorizadoException exception) {
		return respuesta(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class})
	public ResponseEntity<ErrorResponse> manejarSolicitudInvalida(Exception exception) {
		return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> manejarErrorDeIntegridad(DataIntegrityViolationException exception) {
		return respuesta(HttpStatus.CONFLICT, "No se pudo completar la operación.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> manejarErrorInesperado(Exception exception) {
		logger.error("Error inesperado", exception);
		return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.");
	}

	private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String mensaje) {
		return ResponseEntity.status(estado).body(new ErrorResponse(mensaje, List.of()));
	}
}
