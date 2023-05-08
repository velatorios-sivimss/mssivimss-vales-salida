package com.imss.sivimss.valesalida.beans;

import com.google.gson.Gson;
import com.imss.sivimss.valesalida.exception.BadRequestException;
import com.imss.sivimss.valesalida.model.request.*;
import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.util.AppConstantes;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.QueryHelper;
import com.imss.sivimss.valesalida.util.SelectQueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Clase para controlar la l&oacute;gica de negocio para administrar los movimientos de art&iacute;culos
 * asignados a una
 */
@Component
@Slf4j
public class ValeSalida {

    private static final int TIPO_SERVICIO_RENTA_EQUIPO = 2;
    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
    private static final int ESTATUS_ODS_GENERADA = 2;
    private static final int ESTATUS_ELIMINADA = 0;
    private static final int ESTATUS_SALIDA = 1;
    private static final int ESTATUS_ENTRADA = 2;
    // todo - agregar campos para las tablas y usarlos como constantes
    // campos vale_salida, ver como se acomoda mejor usarlos
//    private static final String ID_VALE_SALIDA = "vs.ID_VALE_SALIDA as idValeSalida";
    private static final String ID_VALE_SALIDA = "ID_VALE_SALIDA";
    private static final String FOLIO_ODS = "CVE_FOLIO";
    private final Gson gson;

    public ValeSalida() {
        this.gson = new Gson();
    }

    /**
     * Consulta el detalle de un Vale de Salida.
     *
     * @param id
     * @param idDelegacion
     * @return
     */
    public DatosRequest consultar(long id, Integer idDelegacion) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("vs.ID_VALESALIDA as idValeSalida",
                        "vs.CVE_FOLIO as folioValeSalida",
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "d.DES_DELEGACION as nombreDelegacion",
                        "vs.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as folioOds",
                        "perContratante.NOM_PERSONA as nombreContratante",
                        "perFinado.NOM_PERSONA as nombreFinado",
                        "vs.FEC_SALIDA as fechaSalida",
                        "vs.FEC_ENTRADA as fechaEntrada",
                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
                        "vs.NOM_RESPON_INSTA as nombreResponsableInstalacion",
                        "vs.CVE_MATRICULA_RESINST as matriculaResponsableInstalacion",
                        "vs.NOM_RESPEQUIVELACION as nombreResponsableEquipo",
                        "vs.CVE_MATRICULARESPEQUIVELACION as matriculaResponsableEquipo",
                        "vs.NOM_RESPON_ENTREGA as nombreResponsableEntrega",
                        "vs.CVE_MATRICULA_RESPON as matriculaResponsableEntrega",
                        "vs.CAN_ARTICULOS as totalArticulos",
                        "domicilio.DES_CALLE as calle",
                        "domicilio.NUM_EXTERIOR as numExt",
                        "domicilio.NUM_INTERIOR as numInt",
                        "domicilio.DES_COLONIA as colonia",
                        "cp.DES_ESTADO as estado",
                        "cp.DES_MNPIO as municipio",
                        "cp.CVE_CODIGO_POSTAL as codigoPostal",
                        "dvs.ID_INVENTARIO as idArticulo",
                        "inventario.DES_ARTICULO as nombreArticulo",
                        "dvs.CAN_ARTICULOS as cantidadArticulos",
                        "dvs.DES_OBSERVACION as observaciones")
                .from("SVT_VALE_SALIDA vs")
                .join("SVC_VELATORIO v", "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join("SVC_DELEGACION d", "d.ID_DELEGACION = :idDelegacion")
                .join("SVC_ORDEN_SERVICIO ods",
                        "vs.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO",
                        "ods.ID_ESTATUS_ORDEN_SERVICIO = 2")
                .join("SVC_FINADO usuFinado",
                        "v.ID_VELATORIO = usuFinado.ID_VELATORIO",
                        "usuFinado.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_PERSONA perFinado", "usuFinado.ID_PERSONA = perFinado.ID_PERSONA")
                .join("SVC_CONTRATANTE usuContratante", "ods.ID_CONTRATANTE = usuContratante.ID_CONTRATANTE")
                .join("SVC_PERSONA perContratante", "usuContratante.ID_PERSONA = perContratante.ID_PERSONA")
                .join("SVC_INFORMACION_SERVICIO infoServ", "infoServ.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_INFORMACION_SERVICIO_VELACION infoOds", "infoOds.ID_INFORMACION_SERVICIO = infoServ.ID_INFORMACION_SERVICIO")
                .join("SVT_DOMICILIO domicilio", "domicilio.ID_DOMICILIO = infoOds.ID_DOMICILIO")
                .join("SVC_CP cp", "cp.ID_CODIGO_POSTAL = domicilio.ID_CP")
                .leftJoin("SVT_VALE_SALIDADETALLE dvs",
                        "dvs.ID_VALESALIDA = vs.ID_VALESALIDA", "dvs.ID_ESTATUS = 1").or("dvs.ID_ESTATUS = 2")
                .join("SVT_INVENTARIO inventario", "dvs.ID_INVENTARIO = inventario.ID_INVENTARIO");
        queryUtil.where("vs.ID_VALESALIDA = :idValeSalida",
                        "vs.ID_ESTATUS <> 0")
                .setParameter("idValeSalida", id)
                .setParameter("idDelegacion", idDelegacion);

        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, encoded);
        DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);


        return datos;
    }

    /**
     * Elimina los productos de un vale de salida
     *
     * @param idValeSalida
     * @return
     */
    public DatosRequest cambiarEstatusDetalleValeSalida(Long idValeSalida, Integer idUsuario, int estatus) {

        StringBuilder queryBuilder = new StringBuilder("UPDATE SVT_VALE_SALIDADETALLE SET ");
        queryBuilder.append("ID_ESTATUS = ").append(estatus).append(", ")
                .append("ID_USUARIO_MODIFICA = ").append(idUsuario).append(", ")
                .append("FEC_ACTUALIZACION = ").append(CURRENT_TIMESTAMP).append(" ")
                .append("WHERE ID_VALESALIDA = ").append(idValeSalida)
                .append(" AND ID_ESTATUS = ").append(ESTATUS_SALIDA);

        String query = queryBuilder.toString();

        Map<String, Object> parametros = new HashMap<>();
        DatosRequest datos = new DatosRequest();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        datos.setDatos(parametros);

        return datos;
    }

    /**
     * Consulta los vales de salida generados, recupera la informaci&oacute;n del Vale de salida, as&iacute; como
     * el detalle que representa la lista de art&iacute;culos asociada al Vale de Salida
     *
     * @param request Filtros para hacer la consulta de los vales de salida de acuerdo a los filtros especificados
     * @return
     */
    public DatosRequest consultarValesSalida(DatosRequest request) {
        FiltrosRequest filtros = gson.fromJson(
                String.valueOf(request.getDatos().get(AppConstantes.DATOS)),
                FiltrosRequest.class
        );
        SelectQueryUtil queryUtil = new SelectQueryUtil();

        queryUtil.select("ID_VALESALIDA as idValeSalida",
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "vs.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as folioOds",
                        "vs.FEC_SALIDA as fechaSalida",
                        "vs.FEC_ENTRADA as fechaEntrada",
                        "vs.ID_ESTATUS as idEstatus",
                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
                        "vs.NOM_RESPON_INSTA as nombreResponsableInstalacion",
                        "vs.CAN_ARTICULOS as totalArticulos",
                        "CONCAT(perContratante.NOM_PERSONA, ' ', perContratante.NOM_PRIMER_APELLIDO, ' ', perContratante.NOM_SEGUNDO_APELLIDO) as nombreContratante")
                .from("SVT_VALE_SALIDA vs")
                .join("SVC_VELATORIO v", "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join("SVC_ORDEN_SERVICIO ods", "vs.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_CONTRATANTE usuContratante", "ods.ID_CONTRATANTE = usuContratante.ID_CONTRATANTE")
                .join("SVC_PERSONA perContratante", "usuContratante.ID_PERSONA = perContratante.ID_PERSONA")
                .where("vs.ID_ESTATUS <> 0");

        // todo - falta agregar la delegacion en las validaciones
        if (filtros != null && !filtros.validarNulos()) {
            if (filtros.getIdVelatorio() != null) {
                queryUtil.where("vs.ID_VELATORIO = :idVelatorio")
                        .setParameter("idVelatorio", filtros.getIdVelatorio());
            }
            if (filtros.getFolioOds() != null) {
                queryUtil.where("ods.CVE_FOLIO = :folioOds")
                        .setParameter("folioOds", filtros.getFolioOds());
            }
            if (filtros.getFechaInicio() != null && filtros.getFechaFinal() != null) {
                if (filtros.validarFechas()) {
                    queryUtil.where("vs.FEC_SALIDA >= :fechaInicial",
                                    "vs.FEC_SALIDA <= :fechaFin")
                            .setParameter("fechaInicial", filtros.getFechaInicio())
                            .setParameter("fechaFin", filtros.getFechaFinal());
                } else {
                    // todo - mandar el error de la validacion de las fechas pero hay que hacer la validacion en el servicio
                }
            }
        }
        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
        request.getDatos().put(AppConstantes.QUERY, encoded);
        request.getDatos().remove(AppConstantes.DATOS);
        return request;
    }

    /**
     * Consulta los datos de la ODS, consulta tambi&eacute;n la lista de art&iacute;culos disponibles para ese
     * velatorio.
     *
     * @param request
     * @return
     */
    public DatosRequest consultarDatosOds(ConsultaDatosPantallaRequest request) {
        // todo - validar que los campos no sean nulos
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select(
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "d.DES_DELEGACION as nombreDelegacion",
                        "ods.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as request",
                        "CONCAT(perContratante.NOM_PERSONA, ' ', perContratante.NOM_PRIMER_APELLIDO, ' ', perContratante.NOM_SEGUNDO_APELLIDO) as nombreContratante",
                        "CONCAT(perFinado.NOM_PERSONA, ' ', perFinado.NOM_PRIMER_APELLIDO, ' ', perFinado.NOM_SEGUNDO_APELLIDO) as nombreFinado",
                        "domicilio.DES_CALLE as calle",
                        "domicilio.NUM_EXTERIOR as numExt",
                        "domicilio.NUM_INTERIOR as numInt",
                        "domicilio.DES_COLONIA as colonia",
                        "cp.DES_ESTADO as estado",
                        "cp.DES_MNPIO as municipio",
                        "cp.CVE_CODIGO_POSTAL as codigoPostal",
                        "inventario.ID_INVENTARIO as idArticulo",
                        "inventario.NOM_ARTICULO as nombreArticulo",
                        "inventario.CAN_STOCK as cantidadArticulos")
                .from("SVC_ORDEN_SERVICIO ods")
                .join("SVC_VELATORIO v")
//                .join("SVC_DELEGACION d", "d.ID_DELEGACION = v.ID_DELEGACION")
                .join("SVC_DELEGACION d")
                .join("SVC_FINADO usuFinado",
                        "v.ID_VELATORIO = usuFinado.ID_VELATORIO",
                        "usuFinado.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_PERSONA perFinado", "usuFinado.ID_PERSONA = perFinado.ID_PERSONA")
                .join("SVC_CONTRATANTE usuContratante", "ods.ID_CONTRATANTE = usuContratante.ID_CONTRATANTE")
                .join("SVC_PERSONA perContratante", "usuContratante.ID_PERSONA = perContratante.ID_PERSONA")
                .join("SVC_INFORMACION_SERVICIO infoServ", "infoServ.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_INFORMACION_SERVICIO_VELACION infoOds",
                        "infoOds.ID_INFORMACION_SERVICIO = infoServ.ID_INFORMACION_SERVICIO")
                .join("SVT_DOMICILIO domicilio", "domicilio.ID_DOMICILIO = infoOds.ID_DOMICILIO")
                .join("SVC_CP cp", "cp.ID_CODIGO_POSTAL = domicilio.ID_CP")
                .join("SVT_INVENTARIO inventario",
                        "inventario.ID_VELATORIO = v.ID_VELATORIO",
                        "inventario.ID_TIPO_SERVICIO = " + TIPO_SERVICIO_RENTA_EQUIPO);

        queryUtil.where(
                        "ods.CVE_FOLIO = :folioOds",
                        "ods.ID_ESTATUS_ORDEN_SERVICIO = " + ESTATUS_ODS_GENERADA,
                        "d.ID_DELEGACION = :idDelegacion",
                        "v.ID_VELATORIO = :idVelatorio")
                .setParameter("folioOds", request.getFolioOds())
                .setParameter("idDelegacion", request.getIdDelegacion())
                .setParameter("idVelatorio", request.getIdVelatorio());

        return getDatosRequest(queryUtil);
    }

    /**
     * Recupera el objeto que se estar&aacute; enviando al servicio que ejecuta la consulta.
     *
     * @param queryUtil Instancia de <b>{@code SelectQueryUtil}</b> de donde se va a recuperar el query generado.
     * @return El objeto armado.
     */
    private DatosRequest getDatosRequest(SelectQueryUtil queryUtil) {
        String query = getQuery(queryUtil);
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);

        return datos;
    }

    /**
     * Crea un registro para el vale de salida con su respectivo detalle
     *
     * @param valeSalida
     * @param usuarioDto
     * @return
     */
    public DatosRequest crearVale(ValeSalidaDto valeSalida, UsuarioDto usuarioDto) {
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();

        QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDA");
        queryHelper.agregarParametroValues("ID_VELATORIO", String.valueOf(valeSalida.getIdVelatorio()));
        queryHelper.agregarParametroValues("ID_ORDEN_SERVICIO", String.valueOf(valeSalida.getIdOds()));
        queryHelper.agregarParametroValues("FEC_SALIDA", "STR_TO_DATE('" + valeSalida.getFechaSalida() + "', '%d-%m-%Y')");
        queryHelper.agregarParametroValues("NOM_RESPON_INSTA", "'" + valeSalida.getNombreResponsableInstalacion() + "'");
        queryHelper.agregarParametroValues("CVE_MATRICULA_RESINST", valeSalida.getMatriculaResponsableInstalacion());
        queryHelper.agregarParametroValues("NOM_RESPEQUIVELACION", "'" + valeSalida.getNombreResponsableEquipoVelacion() + "'");
        queryHelper.agregarParametroValues("CVE_MATRICULARESPEQUIVELACION", "'" + valeSalida.getMatriculaResponsableEquipoVelacion() + "'");

        queryHelper.agregarParametroValues("NUM_DIA_NOVENARIO", String.valueOf(valeSalida.getDiasNovenario()));

        queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(valeSalida.getCantidadArticulos()));

        queryHelper.agregarParametroValues("ID_ESTATUS", String.valueOf(ESTATUS_SALIDA));

        queryHelper.agregarParametroValues("ID_USUARIO_ALTA", String.valueOf(usuarioDto.getIdUsuario()));
        queryHelper.agregarParametroValues("FEC_ALTA", CURRENT_TIMESTAMP);

        final List<DetalleValeSalidaRequest> articulos = valeSalida.getArticulos();

        // todo - mover esta validacon al servicio
        if (articulos.isEmpty()) {
            // todo - recuperar el mensaje del catalogo
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "La lista de articulos no puede estar vacia");
        }
        String queriesArticulos = crearDetalleVale(articulos, null);
        String query = queryHelper.obtenerQueryInsertar() + queriesArticulos;
        parametros.put(AppConstantes.QUERY, getBinary(query));
        parametros.put("separador", "$$");
        parametros.put("replace", "idTabla");
        datos.setDatos(parametros);
        return datos;
    }

    /**
     * Crea los registros del detalle del Vale de salida, para que se pueda hacer la relaci&oacute;n entre
     * Vale de Salida y su Detalle.
     *
     * @param articulos
     * @return
     */
    private String crearDetalleVale(List<DetalleValeSalidaRequest> articulos, Long idValeSalida) {
        // todo - recuperar el idUsuario, para pasarlo tambien en este metodo
        StringBuilder query = new StringBuilder();
        for (DetalleValeSalidaRequest detalleValeSalida : articulos) {
            QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDADETALLE");
            final boolean isIdValeSalida = idValeSalida != null;
            queryHelper.agregarParametroValues("ID_VALESALIDA", isIdValeSalida ? String.valueOf(idValeSalida) : "idTabla");
            queryHelper.agregarParametroValues("ID_INVENTARIO", String.valueOf(detalleValeSalida.getIdInventario()));
            queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(detalleValeSalida.getCantidad()));
            queryHelper.agregarParametroValues("DES_OBSERVACION", "'" + detalleValeSalida.getObservaciones() + "'");
            queryHelper.agregarParametroValues("ID_ESTATUS", "1");
            if (!isIdValeSalida) {
                query.append(" $$ ").append(queryHelper.obtenerQueryInsertar());
            }
        }
        return query.toString();
    }

    /**
     * Actualiza los detalles de los vales de salida
     *
     * @param articulos
     * @param idValeSalida
     * @return
     */
    public DatosRequest actualizarDetalleVale(List<DetalleValeSalidaRequest> articulos, Long idValeSalida) {
        StringBuilder query = new StringBuilder();
        for (DetalleValeSalidaRequest detalleValeSalida : articulos) {
            QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDADETALLE");
            queryHelper.agregarParametroValues("ID_VALESALIDA", idValeSalida != null ? String.valueOf(idValeSalida) : "idTabla");
            queryHelper.agregarParametroValues("ID_ARTICULO", String.valueOf(detalleValeSalida.getIdInventario()));
            queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(detalleValeSalida.getCantidad()));
            queryHelper.agregarParametroValues("DES_OBSERVACION", "'" + detalleValeSalida.getObservaciones() + "'");
            query.append(" $$ ").append(queryHelper.obtenerQueryInsertar());
        }
        final DatosRequest datosRequest = new DatosRequest();
        return datosRequest;
    }

    public List<String> generarQueries(String... queries) {

        Map<String, Object> parametros = new HashMap<>();
        final List<String> updates = Arrays.asList(queries);
        parametros.put("updates", updates.stream().map(ValeSalida::getBinary));

        final DatosRequest datosRequest = new DatosRequest();
        return updates;
    }

    /**
     * Modifica un Vale de salida, por si solo se modifican los art&iacute;culos o si se va a registar una entrada.
     *
     * @param valeSalida
     * @param registrarEntrada
     * @return
     */
    public DatosRequest modificarVale(ValeSalidaDto valeSalida, Integer idUsuario, boolean registrarEntrada) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE SVT_VALE_SALIDA ");
        queryBuilder.append("SET ")
                .append("ID_USUARIO_MODIFICA = ").append(idUsuario).append(", ")
                .append("FEC_ACTUALIZACION = ").append(CURRENT_TIMESTAMP).append(", ");

        if (registrarEntrada) {
            queryBuilder.append("FEC_ENTRADA = ").append("STR_TO_DATE('").append(valeSalida.getFechaEntrada()).append("', '%d-%m-%Y'), ")
                    .append("NOM_RESPON_ENTREGA = ").append("'").append(valeSalida.getNombreResponsableEntrega()).append("', ")
                    .append("ID_ESTATUS = ").append(2).append(", ")
                    .append("CVE_MATRICULA_RESPON = ").append("'").append(valeSalida.getMatriculaResponsableEntrega()).append("' ");
        } else {
            queryBuilder.append("CAN_ARTICULOS = ").append(valeSalida.getCantidadArticulos()).append(" ");
        }
        queryBuilder.append("WHERE ID_VALESALIDA = ").append(valeSalida.getIdValeSalida());
        String query = queryBuilder.toString();
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        datos.setDatos(parametros);
        return datos;
    }

    /**
     * Actualiza el inventario del velatorio, para sumar o restar seg&uacute;n corresponda.
     *
     * @param articulo
     * @param restar
     * @return
     */
    public DatosRequest actualizarInventario(DetalleValeSalidaRequest articulo, boolean restar) {
        StringBuilder updateQuery = new StringBuilder("UPDATE SVT_INVENTARIO ");
        updateQuery.append("SET ");
        if (restar) {
            updateQuery.append("CAN_STOCK = CAN_STOCK - ");
        } else {
            updateQuery.append("CAN_STOCK = CAN_STOCK + ");
        }
        updateQuery.append(articulo.getCantidad())
                .append(" ")
                .append("WHERE ID_INVENTARIO = ")
                .append(articulo.getIdInventario());

        return getDatosRequest(updateQuery.toString());
    }

    /**
     * Agrega un nuevo registro al Vale de Salida que se est&eacute; modificando
     *
     * @param articulo
     * @param estatus
     * @return
     */
    public DatosRequest actualizarDetalleValeSalida(Long idValeSalida, Integer idUsuario, DetalleValeSalidaRequest articulo, int estatus) {
        QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDADETALLE");
        queryHelper.agregarParametroValues("ID_VALESALIDA", String.valueOf(idValeSalida));
        queryHelper.agregarParametroValues("ID_INVENTARIO", String.valueOf(articulo.getIdInventario()));
        queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(articulo.getCantidad()));
        queryHelper.agregarParametroValues("DES_OBSERVACION", "'" + articulo.getObservaciones() + "'");
        queryHelper.agregarParametroValues("ID_ESTATUS", String.valueOf(estatus));

        // todo - agregar lo de el usuario que actualiza
        queryHelper.agregarParametroValues("ID_USUARIO_ALTA", String.valueOf(idUsuario));
        queryHelper.agregarParametroValues("FEC_ALTA", CURRENT_TIMESTAMP);

        return getDatosRequest(queryHelper.obtenerQueryInsertar());
    }

    /**
     * Recupera el objeto que se enviar&aacute; en el request.
     *
     * @param string
     * @return
     */
    private DatosRequest getDatosRequest(String string) {
        final String query = string;
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        datos.setDatos(parametros);
        return datos;
    }

    /**
     * Consulta los folios de las &oacute;rdenes de servicio que est&eacute;n activas, para poder llenar el
     * <b>combo</b> en la pantalla para mostrar dichos folios.
     *
     * @return
     */
    public DatosRequest consultarFoliosOds() {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("ID_ORDEN_SERVICIO as idOds",
                        "CVE_FOLIO as folioOds")
                .from("SVC_ORDEN_SERVICIO")
                .where("ID_ESTATUS_ORDEN_SERVICIO = 2");
        return getDatosRequest(queryUtil);
    }

    /**
     * Realiza el llenado de los par&aacute;metros para poder pintar los datos en el servicio de reportes.
     *
     * @param reporteDto
     * @return
     * @throws ParseException
     */
    public Map<String, Object> recuperarDatosFormato(ReporteDto reporteDto) throws ParseException {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("logoImss", "");
        parametros.put("logoSistema", "");
        parametros.put("idValeSalida", reporteDto.getIdValeSalida());
        parametros.put("nombreDelegacion", reporteDto.getNombreDelegacion());
        parametros.put("nombreVelatorio", reporteDto.getNombreVelatorio());
        parametros.put("nombreResponsableEntrega", reporteDto.getNombreResponsableEntrega());
        parametros.put("matriculaResponsableEntrega", reporteDto.getMatriculaResponsableEntrega());
        parametros.put("diasNovenario", reporteDto.getDiasNovenario());
        parametros.put("folioOds", reporteDto.getFolioOds());
        parametros.put("fechaSalida", reporteDto.getFechaSalida());
        parametros.put("domicilio", reporteDto.getDomicilio());
        parametros.put("ciudad", reporteDto.getEstado());
        parametros.put("nombreResponsableInstalacion", reporteDto.getNombreResponsableInstalacion());
        parametros.put("matriculaResponsableInstalacion", reporteDto.getMatriculaResponsableInstalacion());
        parametros.put("condition", "WHERE vsd.`ID_VALESALIDA` = " + reporteDto.getIdValeSalida() + " AND vsd.`ID_ESTATUS` = 2");
        parametros.put("nombreResponsableEquipo", reporteDto.getNombreResponsableEquipo());
        parametros.put("matriculaResponsableEquipo", reporteDto.getMatriculaResponsableEquipo());

        parametros.put("nombreContratante", reporteDto.getNombreContratante());

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "MX"));
        String fechaFormateada = dateFormatter.format(reporteDto.getFechaEntregaTmp());
        String[] arregloFechas = fechaFormateada.split(" ");
        String dia = arregloFechas[0];
        String mes = arregloFechas[1].toUpperCase();
        String anio = arregloFechas[2];
        parametros.put("fechaEntrega", reporteDto.getFechaEntrega());
        parametros.put("diaFechaEntrega", dia);
        parametros.put("mesFechaEntrega", mes);
        parametros.put("anioFechaEntrega", anio);
        parametros.put("ciudad", reporteDto.getEstado());
        parametros.put("rutaNombreReporte", reporteDto.getRuta());
        parametros.put("tipoReporte", reporteDto.getTipoReporte());

        return parametros;
    }


    /**
     * Recupera los datos para poder generar el reporte de la tabla de Vales de Salida
     *
     * @param filtros
     * @return
     * @throws ParseException
     */
    public Map<String, Object> recuperarDatosFormatoTabla(ReporteTablaDto filtros) throws ParseException {
        final Map<String, Object> parametros = new HashMap<>();
        StringBuilder condicionBuilder = new StringBuilder();
        // validar si los filtros son nullos ??
        parametros.put("rutaNombreReporte", filtros.getRuta());
        parametros.put("tipoReporte", filtros.getTipoReporte());
        parametros.put("idValeSalida", filtros.getIdValeSalida());
        parametros.put("idVelatorio", filtros.getIdVelatorio());
        parametros.put("folioOds", filtros.getFolioOds());
        parametros.put("fechaInicio", filtros.getFechaInicio());
        parametros.put("fechaFin", filtros.getFechaFinal());
        return parametros;
    }

    /**
     * Realiza el borrado l&oacute;gico de un registro de Vale de Salida.
     *
     * @param idValeSalida
     * @return
     */
    public DatosRequest cambiarEstatus(Long idValeSalida, Integer idUsuario) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE SVT_VALE_SALIDA ");
        queryBuilder.append("SET ID_ESTATUS = 0 ")
                .append("WHERE ID_VALESALIDA = ").append(idValeSalida);
        String query = queryBuilder.toString();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        final DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);

        return datos;
    }

    /**
     * Recupera la cadena encriptada del query generado.
     *
     * @param query
     * @return
     */
    private static String getBinary(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes());
    }

    /**
     * Recuperar el query cuando se usa la clase: <b>{@code SelectQueryUtil}</b>
     *
     * @param queryUtil
     * @return
     */
    private static String getQuery(SelectQueryUtil queryUtil) {
        return queryUtil.build();
    }

}
