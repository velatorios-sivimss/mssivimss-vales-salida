package com.imss.sivimss.valesalida.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.imss.sivimss.valesalida.beans.ValeSalida;
import com.imss.sivimss.valesalida.model.request.UsuarioDto;
import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.service.ValeSalidaService;
import com.imss.sivimss.valesalida.util.AppConstantes;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.ProviderServiceRestTemplate;
import com.imss.sivimss.valesalida.util.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.text.ParseException;

public class ValesSalidaServiceImpl implements ValeSalidaService {
    private static final String MSG_ERROR_REGISTRAR = "5";
    private static final String SIN_INFORMACION = "87";//No contamos con capillas disponibles por el momento. Intenta mas tarde.
    //    MSG020	La fecha inicial no puede ser mayor que la fecha final.
    private final static String MSG_VALIDACION_FECHAS = "20";
    //    MSG022	Selecciona por favor un criterio de búsqueda.
    private final static String MSG022_CRITERIO_BUSQUEDA = "22";
    //    MSG023	El archivo se guardó correctamente.
    private final static String MSG023_GUARDAR_OK = "23";
    //    MSG064	Error en la descarga del documento. Intenta nuevamente.
    private final static String MSG064_ERROR_DESCARGA_DOC = "064";
    //    MSG085	El número de folio no existe. Verifica tu información.
    private final static String MSG085_ERROR_FOLIO = "085";
    //    MSG095	¿Estás seguro de eliminar el artículo?
    private final static String MSG095_CONFIRMA_ELIMINAR = "95";
    //    MSG120	¿Estás seguro de registrar la salida del equipo de velación?
    private final static String MSG095_CONFIRMAR_REGISTRO_SALIDA = "120";
    //    MSG131	Se ha registrado correctamente el registro de salida del equipo de velación.
    private final static String MSG131_REGISTRO_SALIDA_OK = "131";
    //    MSG132	¿Estás seguro de registrar la entrada del equipo de velación?
    private final static String MSG132_CONFIRMAR_REGISTRO_ENTRADA = "132";
    //    MSG133	Se ha registrado correctamente el registro de entrada del equipo de velación.
    private final static String MSG133_REGISTRO_ENTRADA_OK = "133";

    // endpoints
    @Value("${endpoints.dominio-consulta}")
    private String URL_DOMINIO_CONSULTA;
    @Value("${endpoints.dominio-consulta-paginado}")
    private String URL_DOMINIO_CONSULTA_PAGINADO;
    @Value("${endpoints.dominio-crear}")
    private String URL_DOMINIO_CREAR;
    @Value("${endpoints.dominio-insertar-multiple}")
    private String URL_DOMINIO_INSERTAR_MULTIPLE;
    @Value("${endpoints.dominio-actualizar}")
    private String URL_DOMINIO_ACTUALIZAR;
    @Value("${endpoints.dominio-reportes}")
    private String URL_REPORTES;

    private final ObjectMapper mapper;
    private final ValeSalida valeSalida;
    private final ProviderServiceRestTemplate restTemplate;
    private final Gson gson;

    public ValesSalidaServiceImpl(ObjectMapper mapper, ValeSalida valeSalida, ProviderServiceRestTemplate restTemplate) {
        this.mapper = mapper;
        this.valeSalida = valeSalida;
        this.restTemplate = restTemplate;
        this.gson = new Gson();
    }


    @Override
    public Response<?> crearVale(DatosRequest request, Authentication authentication) throws IOException {

        ValeSalidaDto valeSalidaRequest = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                ValeSalidaDto.class
        );
        UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        final DatosRequest datos = valeSalida.crearVale(valeSalidaRequest, usuarioDto);
        final Response<?> response = restTemplate.consumirServicio(
                datos.getDatos(),
                URL_DOMINIO_INSERTAR_MULTIPLE,
                authentication
        );

        // todo - validar la respuesta para mandar el mensaje que corresponda
        return response;
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
