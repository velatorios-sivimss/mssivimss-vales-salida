package com.imss.sivimss.valesalida.model.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ValeSalidaRequest {
    private Long idVelatorio;
    private String nombreVelatorio;
    // todo - los demas datos salen de la ods
    private long idOds;
    private String folioOds;
    private String folioValeSalida;
    private Long idContratante;
    private String nombreContratante;
    private Long idFinado;
    private String nombreFinado;
    private String matriculaResponsableInstalacion;
    private Long idResponsableInstalacion;
    private String nombreResponsableInstalacion;
    private Long idResponsableEntrega;
    private String matriculaResponsableEntrega;
    private String nombreResponsableEntrega;
    private Long idResponsableEquipoVelacion;
    private String matriculaResponsableEquipoVelacion;
    private String nombreResponsableEquipoVelacion;
    private int diasNovenario;
    private String fechaSalida;
    private String fechaEntrada;
    private String cantidadArticulos;
    private String matriculaUsuarioResponsable;
    private String matriculaUsuarioEntrega;
    // todo - agregar los campos faltantes
    private List<DetalleValeSalidaRequest> articulos;
}
