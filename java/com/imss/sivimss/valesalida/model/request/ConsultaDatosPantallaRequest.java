package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;

@Getter
public class ConsultaDatosPantallaRequest {
    private String folioOds;
    private Long idDelegacion;
    private Long idVelatorio;

    /**
     * Hace la validaci&oacute;n de los datos para realizar la consulta para cargar la informaci&oacute;n
     * de la pantalla del registro del <b>Vale de Salida</b>
     *
     * @return
     */
    public boolean validarDatosConsulta() {
        return folioOds == null &&
                idDelegacion == null &&
                idVelatorio == null;
    }
}
