package com.imss.sivimss.valesalida.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.imss.sivimss.valesalida.beans.ValeSalida;
import com.imss.sivimss.valesalida.exception.BadRequestException;
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
import java.util.*;

@Service
@Slf4j
public class ValeSalidaServiceImpl implements ValeSalidaService {
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
            UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
            final DatosRequest datosRequest = valeSalida.crearVale(valeSalidaRequest, usuarioDto);
            response = restTemplate.consumirServicio(
                    datosRequest.getDatos(),
                    URL_DOMINIO_INSERTAR_MULTIPLE,
                    authentication
            );
            if (response.getCodigo() != 200) {
                // todo - ver como manejar las transacciones por si falla en algun punto, porque no quedaria cuadrado el inventario
                return MensajeResponseUtil.mensajeResponse(response, MSG_ERROR_REGISTRAR);
            }
            actualizarInventario(valeSalidaRequest.getArticulos(), true, authentication);
            return MensajeResponseUtil.mensajeResponse(response, MSG131_REGISTRO_SALIDA_OK);
        } catch (Exception ex) {
            response = new Response<>();
            response.setCodigo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            // todo - manejar la excepcion
            log.error("Ha ocurrido un error al crear el vale de salida");
            return MensajeResponseUtil.mensajeResponse(
                    response,
                    MSG_ERROR_REGISTRAR);
        }
    }


    @Override
    public Response<?> consultarVales(DatosRequest request, Authentication authentication) throws IOException {
        try {
            DatosRequest datosRequest = valeSalida.consultarValesSalida(request);

            Response<?> response = restTemplate.consumirServicio(
                    datosRequest.getDatos(),
                    URL_DOMINIO_CONSULTA_PAGINADO,
                    authentication
            );

            // todo - validar si la consulta regreso algun registro, de lo contrario habra que mandar el mensaje de que no hay registros
            // hay que revisar que es lo que contesta el servicio de mod-catalogo
//            if (response.getDatos() == null) {
//              return MensajeResponseUtil.mensajeConsultaResponse(response, e.getMessage());
//            }
            return MensajeResponseUtil.mensajeResponse(response, "");
        } catch (Exception e) {
            // todo - manejar correctamente las excepciones para mandar el mensaje que corresponda
            // todo - mandar un mensaje de error cuando se consultan los vales
            throw e;
        }
    }

    @Override
    public Response<?> consultarDetalle(DatosRequest request, Authentication authentication) throws IOException {
        try {
            Long idValeSalida = gson.fromJson(
                    String.valueOf(request.getDatos().get("palabra")),
                    Long.class);
            UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);

            DatosRequest datosRequest = valeSalida.consultar(idValeSalida, usuarioDto.getIdDelegacion());

            final Response<?> response = restTemplate.consumirServicio(datosRequest.getDatos(),
                    URL_DOMINIO_CONSULTA,
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


    private List<ValeSalidaResponse> getListaValeSalidaResponse(Response<?> response) {
        final List<?> listaResponse = (ArrayList<?>) response.getDatos();
        final List<ValeSalidaResponse> listaValeSalidaResponse = new ArrayList<>();
        for (Object o : listaResponse) {
            ValeSalidaResponse valeSalidaResponse = mapper.convertValue(o, ValeSalidaResponse.class);
            listaValeSalidaResponse.add(valeSalidaResponse);
        }
        return listaValeSalidaResponse;
    }

    @Override
    public Response<?> consultarDatosPantallaRegistro(DatosRequest request, Authentication authentication) throws IOException {
        // siempre deben de llegar el folio, la delegacion y el velatorio

        // todo - se va a usar para ver si tiene el nivel necesario para poder realizar la consulta
        // hay que recuperar la delegacion del usuario para hacer la consulta
        UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        // todo - retomar la validacion del nivel del usuario para que no se puedan hacer consultas que no correspondan
        // buscar los id de los roles que correspondan para el caso de uso
        // validarNivelUsuario(usuarioDto);

        ConsultaDatosPantallaRequest valeSalidaRequest = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                ConsultaDatosPantallaRequest.class
        );
        // todo - validar que los datos no sean nulos para la consulta
        final DatosRequest datosRequest = valeSalida.consultarDatosOds(valeSalidaRequest);
        final Response<?> response = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                URL_DOMINIO_CONSULTA,
                authentication
        );
        // todo hacer la validacion cuando se regresa algo vacio o sin datos
        return getValidacionResponse(response);
    }

    private Response<?> getValidacionResponse(Response<?> response) {
        final List<ValeSalidaResponse> listaValeSalidaResponse = getListaValeSalidaResponse(response);
        if (listaValeSalidaResponse.isEmpty()) {
            return MensajeResponseUtil.mensajeConsultaResponse(response, "No hay registros en el dia");
        }
        Response<ValeSalidaDto> respuesta = getValeSalidaDtoResponse(response, listaValeSalidaResponse);

        System.out.println(listaValeSalidaResponse.toString());

        return respuesta;
    }

//    private void consultarArticulosInventario(long idVelatorio, Authentication authentication) throws IOException {
//        final DatosRequest datosRequest = valeSalida.consultarProductos(idVelatorio);
//        restTemplate.consumirServicio(
//                datosRequest.getDatos(),
//                URL_DOMINIO_CONSULTA,
//                authentication
//        );
//
//    }

    @Override
    public Response<?> modificarVale(DatosRequest request, Authentication authentication) throws IOException {
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());
        // pasar los datos que llegan en el dto para guardar los datos
        // iterar
        eliminarDetalleValeSalida(valeSalidaDto.getIdValeSalida(), authentication);
        // todo - consultar el vale
        final DatosRequest datosRequest = valeSalida.modificarVale(valeSalidaDto, false);
        restTemplate
//        DatosRequest datosRequest = valeSalida.modificarVale()
        // todo - vamos a mandar a llamar al servicio para eliminar articulos de tabla detalle
        // todo - actualizar el vale_salida
        // todo - registrar los nuevos articulos
        // hacer un for con las peticiones para guardar los articulos y lo del inventario
        // todo - restar el inventario
        return null;
    }

    @Override
    public Response<?> registrarEntrada(DatosRequest request, Authentication authentication) throws IOException {
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());
        valeSalida.modificarVale(valeSalidaDto, true);
        eliminarDetalleValeSalida(valeSalidaDto.getIdValeSalida(), authentication);
        actualizarInventario(valeSalidaDto.getArticulos(), false, authentication);
        return null;
    }

    @Override
    public Response<?> cambiarEstatus(DatosRequest request, Authentication authentication) {
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());
        // todo - cambiar los estatus de los articulos del vale
        final DatosRequest datosRequest = valeSalida.cambiarEstatus(valeSalidaDto.getIdValeSalida());
        restTemplate
        // todo - sumar en el inventario la cantidad de articulos
        // todo - apagar el vale_salida
        return null;
    }

    /**
     * todo - agregar documentacion
     *
     * @param idValeSalida
     * @param authentication
     * @throws IOException
     */
    private void eliminarDetalleValeSalida(Long idValeSalida, Authentication authentication) throws IOException {
        final DatosRequest datosRequest = valeSalida.eliminarArticulosValeSalida(idValeSalida);
        final Response<?> response = restTemplate.consumirServicio(datosRequest.getDatos(),
                URL_DOMINIO_ACTUALIZAR,
                authentication);
        if (response.getCodigo() != 200) {
            // todo - agregar el error adecuadamente
            throw new BadRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar los articulos relacionados");
        }
        // no se puede eliminar de la base de datos, por lo que hay que agregar un campo que sea cve_estatus
        // este campo se va actualizar cuando se tenga que borrar el registro y agregar otro
        // se va a usar el id_inventario para este proceso
        // todo - falta ver como se va a eliminar un registro de la base para agregar los nuevos
//        restTemplate.consumirServicio()
    }

    // todo - este servicio puede ser privado
    @Override
    public Response<?> eliminarArticuloDetalleVale(DatosRequest request) {
        // todo - hay que ir a sumar la cantidad al articulo que se elimine
        return null;
    }

    @Override
    public Response<?> consultarCatalogoOds(DatosRequest request, Authentication authentication) throws IOException {
        final DatosRequest datosRequest = valeSalida.consultarFoliosOds();

        return restTemplate.consumirServicio(datosRequest.getDatos(),
                URL_DOMINIO_CONSULTA,
                authentication
        );
    }

    @Override
    public Response<?> generarReportePdf(DatosRequest request, Authentication authentication) throws IOException, ParseException {
        // todo - tengo que recuperar primero los datos del formato, por lo que hay que usar la consulta del detalle
        // recuperar el idValeSalida
        Long idValeSalida = gson.fromJson(
                String.valueOf(request.getDatos().get("palabra")),
                Long.class);

        // recuperar los datos para el reporte
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReporteDto reporteDto = gson.fromJson(datosJson, ReporteDto.class);

        DatosRequest datosRequest = request;
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("palabra", reporteDto.getIdValeSalida());
        datosRequest.setDatos(parametros);
        final Response<?> response = consultarDetalle(request, authentication);
        ValeSalidaDto datosValeSalida = mapper.convertValue(response.getDatos(), ValeSalidaDto.class);

//        final List<?> listaResponse = (ArrayList<?>) consultarDetalle(request, authentication);
//        final List<ValeSalidaResponse> listaValeSalidaResponse = new ArrayList<>();
//        for (Object o : listaResponse) {
//            ValeSalidaResponse valeSalidaResponse = mapper.convertValue(o, ValeSalidaResponse.class);
//            listaValeSalidaResponse.add(valeSalidaResponse);
//        }
//        final ValeSalidaDto datosValeSalida = response.getDatos();
        // armar la direccion y los nombres completos de los responsables

        // armar el objeto para mandar al servicio de reportes
        // hacer una validacion para los campos, bueno no aplica porque solo se necesita del idValeSalida para gernerlo
//        if (reporteDto.getAnio() == null || reporteDto.getMes() == null || reporteDto.getVelatorio() == null) {
//            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Falta infomación");
//        }
        // consultar los datos para el detalle y pasarlos al reporte para que nada mas los pinte
        // para la tabla
        reporteDto.setNombreDelegacion(datosValeSalida.getNombreDelegacion());
        reporteDto.setNombreVelatorio(datosValeSalida.getNombreVelatorio());
        reporteDto.setNombreResponsableInstalacion(datosValeSalida.getNombreResponsableInstalacion());
        reporteDto.setDiasNovenario(datosValeSalida.getDiasNovenario());
        reporteDto.setFolioOds(datosValeSalida.getFolioOds());
        reporteDto.setDomicilio(datosValeSalida.recuperarDomicilio());
        reporteDto.setArticulos(datosValeSalida.getArticulos());
        reporteDto.setNombreResponsableEquipo(datosValeSalida.getNombreResponsableEquipoVelacion());
        reporteDto.setFechaEntrega(datosValeSalida.getFechaEntrada());
        reporteDto.setFechaSalida(datosValeSalida.getFechaSalida());

        Map<String, Object> parametosReporte = valeSalida.generarReporte(reporteDto);

        return restTemplate.consumirServicioReportes(
                parametosReporte,
                reporteDto.getRuta(),
                reporteDto.getTipoReporte(),
                URL_REPORTES,
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
     * Actualiza el inventario de cada art&iacute;culo.
     *
     * @param articulos
     * @param restar
     * @param authentication
     * @throws IOException
     */
    private void actualizarInventario(List<DetalleValeSalidaRequest> articulos, boolean restar, Authentication authentication) throws IOException {
        for (DetalleValeSalidaRequest articulo : articulos) {
            final DatosRequest datosRequest = valeSalida.actualizarInventario(articulo, restar);

            Response<?> response = restTemplate.consumirServicio(datosRequest.getDatos(),
                    URL_DOMINIO_ACTUALIZAR,
                    authentication
            );
            if (response.getCodigo() != 200) {
                throw new BadRequestException(HttpStatus.BAD_REQUEST, "Error al actualizar el inventario");
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
    private static Response<ValeSalidaDto> getValeSalidaDtoResponse(Response<?> response, List<ValeSalidaResponse> listaValeSalidaResponse) {
        ValeSalidaDto resultado = null;
        List<DetalleValeSalidaRequest> listaArticulos = new ArrayList<>();
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
                resultado.setNombreResponsableEntrega(valeSalidaResponse.getMatriculaResponsableEntrega());
                resultado.setMatriculaResponsableEquipoVelacion(valeSalidaResponse.getMatriculaResponsableEquipo());
                resultado.setNombreResponsableEquipoVelacion(valeSalidaResponse.getNombreResponsableEquipo());

                resultado.setDiasNovenario(valeSalidaResponse.getDiasNovenario());

                resultado.setFechaSalida(valeSalidaResponse.getFechaSalida());
                resultado.setFechaEntrada(valeSalidaResponse.getFechaEntrada());

                resultado.setCantidadArticulos(valeSalidaResponse.getTotalArticulos());
                // todo - agregar la direccion

            }

            DetalleValeSalidaRequest detalleArticulo = new DetalleValeSalidaRequest();
            detalleArticulo.setIdValeSalida(valeSalidaResponse.getIdValeSalida());
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
}
