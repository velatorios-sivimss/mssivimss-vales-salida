package com.imss.sivimss.valesalida.service;

import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.Response;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.text.ParseException;

public interface ValeSalidaService {


    Response<?> crearVale(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> consultarVales(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> consultarDetalle(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> consultarDatosPantallaRegistro(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> modificarVale(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> registrarEntrada(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> cambiarEstatus(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> eliminarArticuloDetalleVale(DatosRequest request);

    Response<?> consultarCatalogoOds(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> generarReportePdf(DatosRequest request, Authentication authentication) throws IOException, ParseException;
}
