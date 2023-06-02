package com.imss.sivimss.valesalida.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValeSalidaResponse {
    private Long idValeSalida;
    private String folioValeSalida;
    private Long idVelatorio;
    private String nombreVelatorio;
    private String nombreDelegacion;
    private Long idOds;
    private String folioOds;
    private String nombreContratante;
    private String nombreFinado;
    private String fechaSalida;
    private String fechaEntrada;
    private Integer diasNovenario;
    private String nombreResponsableInstalacion;
    private String matriculaResponsableInstalacion;
    private String nombreResponsableEquipo;
    private String matriculaResponsableEquipo;
    private String nombreResponsableEntrega;
    private String matriculaResponsableEntrega;
    private Integer totalArticulos;
    private String calle;
    private String numExt;
    private String numInt;
    private String colonia;
    private String estado;
    private String municipio;
    private String codigoPostal;
    private Long idArticulo;
    private String nombreArticulo;
    private Integer cantidadArticulos;
    private String observaciones;

    private Integer validacionDias;

    /**
     * Recupera el domicilio en una sola cadena
     *
     * @return
     */
    public String recuperarDomicilio() {
        return calle + " " +
                numExt + (numInt.isEmpty() ? " " : numInt + " ") + " " +
                colonia + ", " + municipio + ", " + estado + ", " + codigoPostal;
    }
}
