package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;

@Getter
public class DetalleValeSalidaRequest {
    private Long idValeSalida;
    private Long idArticulo;
//    private String nombreArticulo;
    private Integer cantidad;
    private String observaciones;
}
