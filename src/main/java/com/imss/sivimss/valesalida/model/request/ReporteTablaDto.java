package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteTablaDto {
    private Integer idValeSalida;
    private Integer idDelegacion;
    private Integer idVelatorio;
    private String nombreVelatorio;
    private String folioOds;
    private String fechaInicio;
    private String fechaFinal;
    private String ruta;
    private String tipoReporte;

    /**
     * Verifica si todos los elementos en el objeto son nulos.
     *
     * @return <b>{@code true}</b> o <b>{@code false}</b> dependiendo si todas las propiedades del objeto son
     * nulas o no.
     */
    public boolean validarNulos() {
        return
                idDelegacion == null &&
                idVelatorio == null &&
                idValeSalida == null &&
                folioOds == null &&
                fechaInicio == null && fechaFinal == null;
    }
}
