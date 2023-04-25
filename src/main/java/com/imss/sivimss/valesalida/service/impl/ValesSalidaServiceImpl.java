package com.imss.sivimss.valesalida.service.impl;

import com.imss.sivimss.valesalida.service.ValeSalidaService;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.Response;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.text.ParseException;

public class ValesSalidaServiceImpl implements ValeSalidaService {
    @Override
    public Response<?> crearVale(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> consultarVales(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> consultarDetalle(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> consultarDatosPantallaRegistro(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> modificarVale(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> registrarEntrada(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> cambiarEstatus(DatosRequest request, Authentication authentication) {
        return null;
    }

    @Override
    public Response<?> eliminarArticuloDetalleVale(DatosRequest request) {
        return null;
    }

    @Override
    public Response<?> consultarCatalogoOds(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }

    @Override
    public Response<?> generarReportePdf(DatosRequest request, Authentication authentication) throws IOException, ParseException {
        return null;
    }
}
