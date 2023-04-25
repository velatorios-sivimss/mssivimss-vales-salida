package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalleValeSalidaRequest {
    private Long idValeSalida;
    private Long idInventario;
    private String nombreArticulo;
    private Integer cantidad;
    private String observaciones;
}
