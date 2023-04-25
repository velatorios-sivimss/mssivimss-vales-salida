package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReporteDto {
    // todo - agregar los datos que se necesitan par el formato
    private Long idOoad;
    private String nombreDelegacion;
    private Long idVelatorio;
    private String nombreVelatorio;

    private String nombreResponsableEntrega;
    private Integer diasNovenario;
    private String folioOds;
    private String fechaActual;
    private List<DetalleValeSalidaRequest> articulos;
    private String domicilio;
    private String nombreResponsableInstalacion;
    private String nombreResponsableEquipo;
    private String fechaEntrega;
    private String fechaSalida;

    // datos del reporte
    // todo - ver si se puede hacer la condicion
    private Long idValeSalida;
    private String condition; // no se va a usar
    private String ruta;
    private String tipoReporte;
}
