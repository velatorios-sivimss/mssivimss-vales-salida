package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ReporteDto {
    private Long idOoad;
    private String nombreDelegacion;
    private Long idVelatorio;
    private String nombreVelatorio;
    private String nombreContratante;

    private String nombreResponsableEntrega;
    private String matriculaResponsableEntrega;
    private Integer diasNovenario;
    private String folioOds;
    private String fechaActual;
    private List<DetalleValeSalidaRequest> articulos;
    private String domicilio;
    private String estado;
    private String nombreResponsableInstalacion;
    private String matriculaResponsableInstalacion;
    private String nombreResponsableEquipo;
    private String matriculaResponsableEquipo;
    private String fechaEntrega;
    private String fechaSalida;
    private Date fechaEntregaTmp;

    // datos del reporte
    private Long idValeSalida;
    private String condition;
    private String ruta;
    private String tipoReporte;
}
