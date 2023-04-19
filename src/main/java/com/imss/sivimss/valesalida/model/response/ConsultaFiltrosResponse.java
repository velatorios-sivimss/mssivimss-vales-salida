package com.imss.sivimss.valesalida.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultaFiltrosResponse {
    private Long idValeSalida;
    private Long idVelatorio;
    private String nombreVelatorio;
    private Long idOds;
    private String folioOds;
    private String nombreContratante;
    private String fechaSalida;
    private String fechaEntrada;
    private String responsableInstalacion;
}
