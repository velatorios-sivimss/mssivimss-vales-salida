package com.imss.sivimss.valesalida.model.response;

import lombok.Getter;

import java.util.List;

// todo - agregar los campos que le hagan falta al dto
@Getter
public class ValeSalidaDto {
    private long IdValeSalida;
    private String folioValeSalida;
    private long idVelatorio;
    private String nombreVelatorio;
    private long idOds;
    private String folioOds;

    private Long idContratante;
    private String nombreContratante;
    private Long idFinado;
    private String nombreFinado;

    private String nombreResponsableInstalacion;
    private String matriculaResponsableInstalacion;
    private Long idResponsableInstalacion;

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
    private List<ValeSalidaDto> articulos;
}
