package com.imss.sivimss.valesalida.service.impl;

import com.google.gson.Gson;
import com.imss.sivimss.valesalida.beans.ValeSalida;
import com.imss.sivimss.valesalida.exception.BadRequestException;
import com.imss.sivimss.valesalida.model.request.DetalleValeSalidaRequest;
import com.imss.sivimss.valesalida.model.request.FiltrosRequest;
import com.imss.sivimss.valesalida.model.request.UsuarioDto;
import com.imss.sivimss.valesalida.model.request.ValeSalidaRequest;
import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.service.ValeSalidaService;
import com.imss.sivimss.valesalida.util.AppConstantes;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.ProviderServiceRestTemplate;
import com.imss.sivimss.valesalida.util.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ValeSalidaServiceImpl implements ValeSalidaService {
    private final static String ENDPOINT_ACTUALIZAR = "/generico/actualizar";
    private final static String ENDPOINT_PAGINADO = "/generico/paginado";
    private final static String ENDPOINT_CONSULTA = "/generico/consulta";

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

    private final ValeSalida valeSalida;
    private final ProviderServiceRestTemplate restTemplate;
    private final Gson gson;

    public ValeSalidaServiceImpl(ValeSalida valeSalida, ProviderServiceRestTemplate restTemplate) {
        this.valeSalida = valeSalida;
        this.restTemplate = restTemplate;
        this.gson = new Gson();
    }

    @Override
    public Response<?> crearVale(DatosRequest request, Authentication authentication) throws IOException {

        ValeSalidaRequest valeSalidaRequest = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                ValeSalidaRequest.class
        );
        UsuarioDto usuarioDto = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        // todo - registrarEntrada
        //      - crear el registro en vale salida
        //      - recuperar el id del registro que se acaba de insertar
        final DatosRequest datosRequest = valeSalida.crearVale(valeSalidaRequest, usuarioDto);
        // mandar el request para guardar los registros
        //      - crear el registro del detalle del vale
        Response<?> response = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                URL_DOMINIO_INSERTAR_MULTIPLE,
                authentication
        );
        if (response.getCodigo() != 200) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Error al insertar el registro");
        }

        //      - descontar el stock del inventario
        actualizarInventario(valeSalidaRequest.getArticulos(), true, authentication);
        //      - manejar la excepcion
        return response;
    }


    @Override
    public Response<?> consultarVales(DatosRequest request, Authentication authentication) throws IOException {

        FiltrosRequest filtros = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                FiltrosRequest.class
        );
        DatosRequest datosRequest = valeSalida.consultarValesSalida(filtros);

        final Response<?> response = restTemplate.consumirServicio(
                datosRequest.getDatos(),
                URL_DOMINIO_CONSULTA_PAGINADO,
                authentication
        );
        System.out.println(response);
        return response;
    }

    @Override
    public Response<?> consultarDetalle(DatosRequest request, Authentication authentication) throws IOException {
        Long idValeSalida = getIdValeSalida(request.getDatos());
        DatosRequest datosRequest = valeSalida.consultar(idValeSalida);

        // todo - agregar el manejo de las excepciones
        return restTemplate.consumirServicio(datosRequest.getDatos(),
                URL_DOMINIO_CONSULTA,
                authentication
        );
    }


    @Override
    public Response<?> consultarDatosPantallaRegistro(DatosRequest request, Authentication authentication) throws IOException {
        // todo - recuperar el folio de la ods y consultar los datos del contratante, finado y direccion
        String folioOds = gson.fromJson(
                String.valueOf(request.getDatos().get("palabra")),
                String.class);
        // todo - hacer una consulta para recuperar los articulos del inventario
        ValeSalidaDto valeSalidaDto = getValeSalida(request.getDatos());
        // todo - guardar el resultado de la consulta en una lista
        consultarArticulosInventario(valeSalidaDto.getIdVelatorio(), authentication);
        return null;
    }

    private void consultarArticulosInventario(long idVelatorio, Authentication authentication) throws IOException {
        final DatosRequest datosRequest = valeSalida.consultarProductos(idVelatorio);
        restTemplate.consumirServicio(
                datosRequest.getDatos(),
                URL_DOMINIO_CONSULTA,
                authentication
        );

    }

    @Override
    public Response<?> modificarVale(DatosRequest request, Authentication authentication) {
        Long idValeSalida = getIdValeSalida(request.getDatos());
        // todo - consultar el vale
        valeSalida.consultarValeSalida();
//        DatosRequest datosRequest = valeSalida.modificarVale()
        // todo - vamos a mandar a llamar al servicio para eliminar articulos de tabla detalle
        // todo - actualizar el vale_salida
        // todo - registrar los nuevos articulos
        // hacer un for con las peticiones para guardar los articulos y lo del inventario
        // todo - restar el inventario
        return null;
    }

    @Override
    public Response<?> eliminarArticuloDetalleVale(DatosRequest request) {
        // todo - hay que ir a sumar la cantidad al articulo que se elimine
        return null;
    }

    @Override
    public Response<?> consultarCatalogoOds(Authentication authentication) throws IOException {
        final DatosRequest datosRequest = valeSalida.consultarFoliosOds();
        restTemplate.consumirServicio(datosRequest.getDatos(),
                URL_DOMINIO_CONSULTA,
                authentication
        );
        return null;
    }

    /**
     * todo - agregar documentacion
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
     * todo - agregar documentacion
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
}
