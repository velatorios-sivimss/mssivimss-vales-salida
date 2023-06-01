package com.imss.sivimss.valesalida.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.imss.sivimss.valesalida.beans.ValeSalida;
import com.imss.sivimss.valesalida.exception.BadRequestException;
import com.imss.sivimss.valesalida.exception.ValidacionFechasException;
import com.imss.sivimss.valesalida.model.request.*;
import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.model.response.ValeSalidaResponse;
import com.imss.sivimss.valesalida.service.ValeSalidaService;
import com.imss.sivimss.valesalida.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class ValeSalidaServiceImpl implements ValeSalidaService {
    private static final int ESTATUS_ELIMINADO = 0;

    private static final int ESTATUS_ENTREGADO = 2;
    private static final String MSG_ERROR_REGISTRAR = "5";
    private static final String MSG023_GUARDAR_OK = "23";
    //    MSG131	Se ha registrado correctamente el registro de salida del equipo de velación.
    private static final String MSG131_REGISTRO_SALIDA_OK = "131";
    //    MSG133	Se ha registrado correctamente el registro de entrada del equipo de velación.
    private static final String MSG133_REGISTRO_ENTRADA_OK = "133";

    // endpoints
    @Value("${endpoints.rutas.dominio-consulta}")
    private String urlDominioConsulta;
    @Value("${endpoints.rutas.dominio-consulta-paginado}")
    private String urlDominioConsultaPaginado;
    @Value("${endpoints.rutas.dominio-crear}")
    private String urlDominioCrear;
    @Value("${endpoints.rutas.dominio-insertar-multiple}")
    private String urlDominioInsertarMultiple;
    @Value("${endpoints.rutas.dominio-actualizar}")
    private String urlDominioActualizar;
    @Value("${endpoints.rutas.dominio-generar-reportes}")
    private String urlReportes;

    private final ObjectMapper mapper;
    private final ValeSalida valeSalida;
    private final ProviderServiceRestTemplate restTemplate;
    private final Gson gson;

    public ValeSalidaServiceImpl(ObjectMapper mapper, ValeSalida valeSalida, ProviderServiceRestTemplate restTemplate) {
        this.mapper = mapper;
        this.valeSalida = valeSalida;
        this.restTemplate = restTemplate;
        this.gson = new Gson();
    }

    @Override
    public Response<?> crearVale(DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = null;
        try {
            ValeSalidaDto valeSalidaRequest = gson.fromJson(
                    String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                    ValeSalidaDto.class
            );
            if (valeSalidaRequest.validarDatosInsert()) {
                throw new BadRequestException(HttpStatus.BAD_REQUEST, "Datos incorrectos, favor de revisar los datos");
            }
            if (valeSalidaRequest.getArticulos().isEmpty()) {
                throw new BadRequestException(HttpStatus.BAD_REQUEST, "La lista de articulos no puede estar vacia");
            }
            UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
            final DatosRequest datosRequest = valeSalida.crearVale(valeSalidaRequest, usuarioDto);
            response = restTemplate.consumirServicio(
                    datosRequest.getDatos(),
                    urlDominioInsertarMultiple,
                    authentication
            );
            if (response.getCodigo() != 200) {
                return MensajeResponseUtil.mensajeResponse(response, MSG_ERROR_REGISTRAR);
            }
            return MensajeResponseUtil.mensajeResponse(response, MSG131_REGISTRO_SALIDA_OK);
        } catch (Exception ex) {
            response = new Response<>();
            response.setCodigo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error("Ha ocurrido un error al crear el vale de salida");
            return MensajeResponseUtil.mensajeResponse(
                    response,
                    MSG_ERROR_REGISTRAR);
        }
    }


    @Override
    public Response<?> consultarVales(DatosRequest request, Authentication authentication) throws IOException {
        try {

            FiltrosRequest filtros = gson.fromJson(
                    String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                    FiltrosRequest.class
            );
            DatosRequest datosRequest = valeSalida.consultarValesSalida(request, filtros);

            Response<?> response = restTemplate.consumirServicio(
                    datosRequest.getDatos(),
                    urlDominioConsultaPaginado,
                    authentication
            );

            if (response.getDatos() == null) {
                return MensajeResponseUtil.mensajeConsultaResponse(response, "No hay registros en el dia");
            }
            return MensajeResponseUtil.mensajeResponse(response, "");
        } catch (ValidacionFechasException e) {
            final Response<?> response = new Response<>();
            response.setCodigo(HttpStatus.OK.value());
            response.setError(true);
            return MensajeResponseUtil.mensajeResponse(response, e.getMensaje());
        }
    }

    @Override
    public Response<?> consultarValesFiltros(DatosRequest request, Authentication authentication) throws IOException {
        return consultarVales(request, authentication);
    }

    @Override
    public Response<?> consultarDetalle(DatosRequest request, Authentication authentication) throws IOException {
        try {
            Long idValeSalida = gson.fromJson(
                    String.valueOf(request.getDatos().get("id")),
                    Long.class);
            UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);

            DatosRequest datosRequest = valeSalida.consultar(idValeSalida, usuarioDto.getIdDelegacion());

            final Response<?> response = restTemplate.consumirServicio(datosRequest.getDatos(),
                    urlDominioConsulta,
                    authentication
            );
            if (response.getCodigo() != 200) {
                return MensajeResponseUtil.mensajeConsultaResponse(response, "agregar mensaje");
            }

            return getValidacionResponse(response);
        } catch (Exception e) {
            throw new BadRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar el detalle del vale");
        }
    }

    @Override
    public Response<?> consultarDatosPantallaRegistro(DatosRequest request, Authentication authentication) throws IOException, ParseException {

        ConsultaDatosPantallaRequest valeSalidaRequest = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                ConsultaDatosPantallaRequest.class
        );
        if (valeSalidaRequest.validarDatosConsulta()) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Los datos nos son correctos, favor de revisarlos.");
        }
        final DatosRequest datosRequest = valeSalida.consultarDatosOds(valeSalidaRequest);
        final Response<?> response = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                urlDominioConsulta,
                authentication
        );
        return getValidacionResponse(response);
    }

    private Response<?> getValidacionResponse(Response<?> response) throws ParseException {
        final List<ValeSalidaResponse> listaValeSalidaResponse = getListaValeSalidaResponse(response);
        if (listaValeSalidaResponse.isEmpty()) {
            return MensajeResponseUtil.mensajeConsultaResponse(response, "45");
        }
        Response<ValeSalidaDto> respuesta = getValeSalidaDtoResponse(response, listaValeSalidaResponse);

        log.info("Se han recuperado correctamente los datos");

        return respuesta;
    }

    @Override
    public Response<?> modificarVale(DatosRequest request, Authentication authentication) throws IOException {
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());

        UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        Integer idUsuario = usuarioDto.getIdUsuario();
        cambiarEstatusDetalleVale(
                valeSalidaDto.getIdValeSalida(),
                idUsuario,
                ESTATUS_ELIMINADO,
                authentication);
        final DatosRequest datosRequest = valeSalida.modificarVale(valeSalidaDto, idUsuario, false);

        final Response<?> responseModificarVale = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                urlDominioActualizar,
                authentication
        );
        if (responseModificarVale.getError()) {
            return MensajeResponseUtil.mensajeResponse(responseModificarVale, "");
        }

        actualizarDetalleValeSalida(valeSalidaDto.getIdValeSalida(), idUsuario, valeSalidaDto.getArticulos(), 1, authentication);
        return MensajeResponseUtil.mensajeResponse(responseModificarVale, MSG023_GUARDAR_OK);
    }

    @Override
    public Response<?> registrarEntrada(DatosRequest request, Authentication authentication) throws IOException {
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());
        UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        final Integer idUsuario = usuarioDto.getIdUsuario();
        final DatosRequest datosRequest = valeSalida.modificarVale(valeSalidaDto, idUsuario, true);
        final Response<?> response = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                urlDominioActualizar,
                authentication
        );

        if (response.getError()) {
            return restTemplate.validarResponse(response);
        }
        cambiarEstatusDetalleVale(valeSalidaDto.getIdValeSalida(), idUsuario, ESTATUS_ENTREGADO, authentication);
        return MensajeResponseUtil.mensajeResponse(response, MSG133_REGISTRO_ENTRADA_OK);
    }

    @Override
    public Response<?> cambiarEstatus(DatosRequest request, Authentication authentication) throws IOException {
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());
        UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        final Integer idUsuario = usuarioDto.getIdUsuario();
        final Long idValeSalida = valeSalidaDto.getIdValeSalida();
        final DatosRequest datosRequest = valeSalida.cambiarEstatus(idValeSalida, idUsuario);
        final Response<?> response = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                urlDominioActualizar,
                authentication
        );
        if (response.getError()) {
            return MensajeResponseUtil.mensajeResponse(response, "");
        }
        cambiarEstatusDetalleVale(idValeSalida, idUsuario, ESTATUS_ELIMINADO, authentication);
        return MensajeResponseUtil.mensajeResponse(response, "Se ha eliminado correctamente");
    }

    /**
     * Cambia el estatus de un Detalle Vale Salida de acuerdo al estatus que se mande:
     * - 0 - Eliminado
     * - 1 - Salida
     * - 2 - Entrada
     *
     * @param idValeSalida
     * @param authentication
     * @throws IOException
     */
    private void cambiarEstatusDetalleVale(Long idValeSalida, Integer idUsuario, int estatus, Authentication authentication) throws IOException {
        final DatosRequest datosRequest = valeSalida.cambiarEstatusDetalleValeSalida(idValeSalida, idUsuario, estatus);
        final Response<?> response = restTemplate.consumirServicio(datosRequest.getDatos(),
                urlDominioActualizar,
                authentication);
        if (response.getCodigo() != 200) {
            throw new BadRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar los articulos relacionados");
        }
    }

    @Override
    public Response<?> consultarCatalogoOds(DatosRequest request, Authentication authentication) throws IOException {
        ConsultaFoliosRequest datosConsulta = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                ConsultaFoliosRequest.class
        );
        final DatosRequest datosRequest = valeSalida.consultarFoliosOds(datosConsulta);

        return restTemplate.consumirServicio(datosRequest.getDatos(),
                urlDominioConsulta,
                authentication
        );
    }

    @Override
    public Response<?> generarReportePdf(DatosRequest request, Authentication authentication) throws IOException, ParseException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReporteDto reporteDto = gson.fromJson(datosJson, ReporteDto.class);

        DatosRequest datosRequest = request;
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("id", reporteDto.getIdValeSalida());
        datosRequest.setDatos(parametros);
        final Response<?> response = consultarDetalle(request, authentication);
        ValeSalidaDto datosValeSalida = mapper.convertValue(response.getDatos(), ValeSalidaDto.class);

        reporteDto.setNombreDelegacion(datosValeSalida.getNombreDelegacion());
        reporteDto.setNombreVelatorio(datosValeSalida.getNombreVelatorio());

        reporteDto.setNombreResponsableEntrega(datosValeSalida.getNombreResponsableEntrega());
        reporteDto.setMatriculaResponsableEntrega(datosValeSalida.getMatriculaResponsableEntrega());

        reporteDto.setNombreResponsableInstalacion(datosValeSalida.getNombreResponsableInstalacion());
        reporteDto.setMatriculaResponsableInstalacion(datosValeSalida.getMatriculaResponsableInstalacion());

        reporteDto.setNombreResponsableEquipo(datosValeSalida.getNombreResponsableEquipoVelacion());
        reporteDto.setMatriculaResponsableEquipo(datosValeSalida.getMatriculaResponsableEquipoVelacion());

        reporteDto.setDiasNovenario(datosValeSalida.getDiasNovenario());
        reporteDto.setFolioOds(datosValeSalida.getFolioOds());
        reporteDto.setDomicilio(datosValeSalida.recuperarDomicilio());
        reporteDto.setEstado(datosValeSalida.getEstado());

        reporteDto.setArticulos(datosValeSalida.getArticulos());

        reporteDto.setFechaEntrega(datosValeSalida.getFechaEntrada());
        reporteDto.setFechaEntregaTmp(datosValeSalida.getFechaEntradaTmp());
        reporteDto.setFechaSalida(datosValeSalida.getFechaSalida());
        reporteDto.setNombreContratante(datosValeSalida.getNombreContratante());

        Map<String, Object> parametosReporte = valeSalida.recuperarDatosFormato(reporteDto);

        final Response<?> respuestaReporte = restTemplate.consumirServicioReportes(
                parametosReporte,
                reporteDto.getRuta(),
                reporteDto.getTipoReporte(),
                urlReportes,
                authentication
        );
        return respuestaReporte;

    }

    @Override
    public Response<?> generarReporteTabla(DatosRequest request, Authentication authentication) throws IOException, ParseException {

        ReporteTablaDto filtros = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                ReporteTablaDto.class
        );
        Map<String, Object> parametrosReporte = valeSalida.recuperarDatosFormatoTabla(filtros);
        return restTemplate.consumirServicioReportes(
                parametrosReporte,
                filtros.getRuta(),
                filtros.getTipoReporte(),
                urlReportes,
                authentication
        );
    }

    /**
     * Recupera el objeto para poder hacer consultas y/o inserts a la base de datos.
     *
     * @param request
     * @return
     */
    private ValeSalidaDto getValeSalida(Map<String, Object> request) {
        return gson.fromJson(
                String.valueOf(request.get(AppConstantes.DATOS)),
                ValeSalidaDto.class
        );
    }

    /**
     * Recupera el <b>{@code idValeSalida}</b> del objeto request.
     *
     * @param request
     * @return
     */
    private Long getIdValeSalida(Map<String, Object> request) {
        return getValeSalida(request).getIdValeSalida();
    }

    /**
     * Actualiza el detalle del vale de salida
     *
     * @param idValeSalida
     * @param articulos
     * @param estatus
     * @param authentication
     * @throws IOException
     */
    private void actualizarDetalleValeSalida(Long idValeSalida, Integer idUsuario, List<DetalleValeSalidaRequest> articulos, int estatus, Authentication authentication) throws IOException {
        for (DetalleValeSalidaRequest articulo : articulos) {
            final DatosRequest datosRequest = valeSalida.actualizarDetalleValeSalida(
                    idValeSalida,
                    idUsuario,
                    articulo,
                    estatus
            );

            Response<?> response = restTemplate.consumirServicio(datosRequest.getDatos(),
                    urlDominioCrear,
                    authentication
            );
            if (response.getCodigo() != 200) {
                throw new BadRequestException(HttpStatus.BAD_REQUEST, "Error al actualizar el detalle vale salida");
            }
        }
    }

    /**
     * Recupera el dto para presentar en la pantalla.
     *
     * @param response
     * @param listaValeSalidaResponse
     * @return
     */
    private static Response<ValeSalidaDto> getValeSalidaDtoResponse(Response<?> response, List<ValeSalidaResponse> listaValeSalidaResponse) throws ParseException {
        ValeSalidaDto resultado = null;
        List<DetalleValeSalidaRequest> listaArticulos = new ArrayList<>();
        final Locale locale = new Locale("es", "MX");
        final SimpleDateFormat fechaSalidaFormatter = new SimpleDateFormat("dd-MM-yyyy", locale);
        for (ValeSalidaResponse valeSalidaResponse : listaValeSalidaResponse) {
            if (resultado == null) {
                resultado = new ValeSalidaDto();
                resultado.setIdValeSalida(valeSalidaResponse.getIdValeSalida());
                resultado.setFolioValeSalida(valeSalidaResponse.getFolioValeSalida());
                resultado.setIdVelatorio(valeSalidaResponse.getIdVelatorio());
                resultado.setNombreVelatorio(valeSalidaResponse.getNombreVelatorio());
                resultado.setNombreDelegacion(valeSalidaResponse.getNombreDelegacion());
                resultado.setIdOds(valeSalidaResponse.getIdOds());
                resultado.setFolioOds(valeSalidaResponse.getFolioOds());

                resultado.setNombreContratante(valeSalidaResponse.getNombreContratante());
                resultado.setNombreFinado(valeSalidaResponse.getNombreFinado());

                resultado.setNombreResponsableInstalacion(valeSalidaResponse.getNombreResponsableInstalacion());
                resultado.setMatriculaResponsableInstalacion(valeSalidaResponse.getMatriculaResponsableInstalacion());
                resultado.setMatriculaResponsableEntrega(valeSalidaResponse.getMatriculaResponsableEntrega());
                resultado.setNombreResponsableEntrega(valeSalidaResponse.getNombreResponsableEntrega());
                resultado.setMatriculaResponsableEquipoVelacion(valeSalidaResponse.getMatriculaResponsableEquipo());
                resultado.setNombreResponsableEquipoVelacion(valeSalidaResponse.getNombreResponsableEquipo());

                resultado.setDiasNovenario(valeSalidaResponse.getDiasNovenario());
                final String patternFecha = "yyyy-MM-dd";
                if (valeSalidaResponse.getFechaSalida() != null) {
                    resultado.setFechaSalida(fechaSalidaFormatter.format(new SimpleDateFormat(patternFecha).parse(valeSalidaResponse.getFechaSalida())));
                }
                if (valeSalidaResponse.getFechaEntrada() != null) {
                    resultado.setFechaEntrada(fechaSalidaFormatter.format(new SimpleDateFormat(patternFecha).parse(valeSalidaResponse.getFechaEntrada())));
                    resultado.setFechaEntradaTmp(new SimpleDateFormat(patternFecha).parse(valeSalidaResponse.getFechaEntrada()));
                }

                resultado.setCantidadArticulos(valeSalidaResponse.getTotalArticulos());
                resultado.setCalle(valeSalidaResponse.getCalle());
                resultado.setNumExt(valeSalidaResponse.getNumExt());
                resultado.setNumInt(valeSalidaResponse.getNumInt());
                resultado.setColonia(valeSalidaResponse.getColonia());
                resultado.setMunicipio(valeSalidaResponse.getMunicipio());
                resultado.setEstado(valeSalidaResponse.getEstado());
                resultado.setCp(valeSalidaResponse.getCodigoPostal());

            }

            DetalleValeSalidaRequest detalleArticulo = new DetalleValeSalidaRequest();
            detalleArticulo.setIdValeSalida(valeSalidaResponse.getIdValeSalida());
            detalleArticulo.setNombreArticulo(valeSalidaResponse.getNombreArticulo());
            detalleArticulo.setIdInventario(valeSalidaResponse.getIdArticulo());
            detalleArticulo.setCantidad(valeSalidaResponse.getCantidadArticulos());
            detalleArticulo.setObservaciones(valeSalidaResponse.getObservaciones());
            listaArticulos.add(detalleArticulo);
        }

        Response<ValeSalidaDto> respuesta = new Response<>();
        if (resultado != null) {
            resultado.setArticulos(listaArticulos);
            respuesta.setCodigo(response.getCodigo());
            respuesta.setMensaje(response.getMensaje());
            respuesta.setError(response.getError());
            respuesta.setDatos(resultado);
        }
        return respuesta;
    }

    /**
     * Recupera la lista del Vale de salida de la respuesta del <b>ms-mod-catalogos</b>
     *
     * @param response
     * @return
     */
    private List<ValeSalidaResponse> getListaValeSalidaResponse(Response<?> response) {
        final List<?> listaResponse = (ArrayList<?>) response.getDatos();
        final List<ValeSalidaResponse> listaValeSalidaResponse = new ArrayList<>();
        for (Object o : listaResponse) {
            ValeSalidaResponse valeSalidaResponse = mapper.convertValue(o, ValeSalidaResponse.class);
            listaValeSalidaResponse.add(valeSalidaResponse);
        }
        return listaValeSalidaResponse;
    }
}
