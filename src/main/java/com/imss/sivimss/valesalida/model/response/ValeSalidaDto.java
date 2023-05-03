package com.imss.sivimss.valesalida.model.response;

import com.imss.sivimss.valesalida.model.request.DetalleValeSalidaRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

// todo - agregar los campos que le hagan falta al dto
@Getter
@Setter
public class ValeSalidaDto {
    private Long IdValeSalida;
    private String folioValeSalida;
    private Long idVelatorio;
    private String nombreVelatorio;
    private String nombreDelegacion;
    private Long idOds;
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

    private Integer diasNovenario;
    private String fechaSalida;
    private String fechaEntrada;
    private Date fechaEntradaTmp;

    private Integer cantidadArticulos;

    // domicilio
    private String calle;
    private String numExt;
    private String numInt;
    private String colonia;
    private String municipio;
    private String estado;
    private String cp;

    private List<DetalleValeSalidaRequest> articulos;

    public String recuperarDomicilio() {
        return calle + " " + numExt + (numInt != null ? numInt + ", " : ", ") + colonia + ", " + municipio + ", " + estado + ", " + cp;
    }
}
