package com.imss.sivimss.arquetipo.util;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatosReporteDTO {
	private Map<String, Object> datos;
	private String nombreReporte;
	private String tipoReporte;

}