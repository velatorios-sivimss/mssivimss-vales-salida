package com.imss.sivimss.valesalida.exception;

import org.springframework.http.HttpStatus;

/**
 * Clase principal para manejar las excepciones BadRequestException de la aplicacion
 *
 * @author Pablo Nolasco
 * @puesto dev
 * @date abril. 2023
 */
public class ValidacionFechasException extends BadRequestException {

	private static final long serialVersionUID = 1L;


	public ValidacionFechasException(HttpStatus codigo, String mensaje) {
		super(codigo, mensaje);
	}

}