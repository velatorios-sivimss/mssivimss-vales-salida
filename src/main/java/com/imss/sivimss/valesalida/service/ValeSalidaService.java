package com.imss.sivimss.valesalida.service;

import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.Response;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.text.ParseException;

public interface ValeSalidaService {


    Response<?> crearVale(DatosRequest request, Authentication authentication) throws IOException;

    // todo - consultarPorFiltros
    Response<?> consultarVales(DatosRequest request, Authentication authentication) throws IOException;

    // todo - consulta detalle vale salida,
    //      - esta consulta puede servir para lo de los formatos
    Response<?> consultarDetalle(DatosRequest request, Authentication authentication) throws IOException;

    // todo - consulta datos ods, para llenar la pantalla de registro de vale de salida
    //      - la consulta se hace por el folio o idOds que me llegue
    //      - recupera datos de finado, contratante y responsable, se recuperan de la tabla usuario
    //      - direccion del servicio, se recupera de la tabla svc_informacion_servicio_velacion
    Response<?> consultarDatosPantallaRegistro(DatosRequest request, Authentication authentication) throws IOException;

    // - modificar vale, solo se modifica lo nuevo que se mande, hay que eliminar los registros del detalle y cargarlos de nuez
    //   y hay que modificar el stock
    //   son estos los caminos posibles:
    //     - solo se modifican las cantidades de los articulos
    //     - se modifican cantidades y observaciones de los articulos
    //     - solo se modifican las observaciones
    //     - se elimina articulo del detalle del vale
    Response<?> modificarVale(DatosRequest request, Authentication authentication) throws IOException;
    Response<?> registrarEntrada(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> cambiarEstatus(DatosRequest request, Authentication authentication);

    // todo - eliminar articulo del detalle del vale de salida
    //      - se elimina el registro y se ajusta el stock
    //        - primero hay que sumar el stock al inventario, luego
    Response<?> eliminarArticuloDetalleVale(DatosRequest request);

    Response<?> consultarCatalogoOds(DatosRequest request, Authentication authentication) throws IOException;
    Response<?> generarReportePdf(DatosRequest request, Authentication authentication) throws IOException, ParseException;
}
