package com.imss.sivimss.valesalida.beans;

import com.imss.sivimss.valesalida.exception.ValidacionFechasException;
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
    private static final String ALIAS_ODS = "ods";
    private static final String ALIAS_USU_CONTRATANTE = "usuContratante";
    private static final String ALIAS_PER_CONTRATANTE = "perContratante";
    private static final String ALIAS_PER_FINADO = "perFinado";
    private static final String ALIAS_USU_FINADO = "usuFinado";
    private static final String ALIAS_NOMBRE_CONTRATANTE = "nombreContratante";
    private static final String ALIAS_NOMBRE_FINADO = "nombreFinado";

    private static final int TIPO_SERVICIO_RENTA_EQUIPO = 2;
    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
    private static final int ESTATUS_ODS_GENERADA = 2;
    private static final int ESTATUS_ELIMINADA = 0;
    private static final int ESTATUS_SALIDA = 1;
    private static final int ESTATUS_ENTRADA = 2;
    private static final String ID_VALE_SALIDA = "ID_VALESALIDA";
    private static final String ID_CONTRATANTE = "ID_CONTRATANTE";
    private static final String ID_PERSONA = "ID_PERSONA";
    private static final String ID_ORDEN_SERVICIO = "ID_ORDEN_SERVICIO";
    private static final String ID_ESTATUS = "ID_ESTATUS";
    private static final String NOM_PERSONA = "NOM_PERSONA";
    private static final String NOM_PRIMER_APELLIDO = "NOM_PRIMER_APELLIDO";
    private static final String NOM_SEGUNDO_APELLIDO = "NOM_SEGUNDO_APELLIDO";
    private static final String ID_USUARIO_ALTA = "ID_USUARIO_ALTA";
    private static final String FEC_ALTA = "FEC_ALTA";
    private static final String CAN_ARTICULOS = "CAN_ARTICULOS";

    // params
    private static final String PARAM_ID_VELATORIO = "idVelatorio";
    private static final String PARAM_ID_VALE_SALIDA = "idValeSalida";
    private static final String PARAM_ID_DELEGACION = "idDelegacion";
    private static final String PARAM_FOLIO_ODS = "folioOds";

    // tablas
    private static final String SVC_VELATORIO = "SVC_VELATORIO";
    private static final String SVC_DELEGACION = "SVC_DELEGACION";
    private static final String SVC_ORDEN_SERVICIO = "SVC_ORDEN_SERVICIO";
    private static final String SVC_CONTRATANTE = "SVC_CONTRATANTE";
    private static final String SVC_PERSONA = "SVC_PERSONA";
    private static final String SVC_INFORMACION_SERVICIO = "SVC_INFORMACION_SERVICIO";
    private static final String SVC_INFORMACION_SERVICIO_VELACION = "SVC_INFORMACION_SERVICIO_VELACION";
    private static final String SVT_DOMICILIO = "SVT_DOMICILIO";
    private static final String SVC_CP = "SVC_CP";
    private static final String ID_VELATORIO = "ID_VELATORIO";
    private static final String NOM_VELATORIO = "NOM_VELATORIO";
    private static final String FOLIO_ODS = "CVE_FOLIO";


    /**
     * Consulta el detalle de un Vale de Salida.
     *
     * @param id
     * @param idDelegacion
     * @return
     */
    public DatosRequest consultar(long id, Integer idDelegacion) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();

        queryUtil.select("vs." + ID_VALE_SALIDA + " as idValeSalida",
                        "vs.CVE_FOLIO as folioValeSalida",
                        "v." + ID_VELATORIO + " as idVelatorio",
                        "v." + NOM_VELATORIO + " as nombreVelatorio",
                        "d.DES_DELEGACION as nombreDelegacion",
                        "vs." + ID_ORDEN_SERVICIO + " as idOds",
                        ALIAS_ODS + "." + FOLIO_ODS + " as folioOds",
                        recuperaNombre(ALIAS_PER_CONTRATANTE, ALIAS_NOMBRE_CONTRATANTE),
                        recuperaNombre(ALIAS_PER_FINADO, ALIAS_NOMBRE_FINADO),
//                        "perFinado." + NOM_PERSONA + " as nombreFinado",
//                        "CONCAT(" + ALIAS_PER_CONTRATANTE + "." + NOM_PERSONA + ", ' ', " + ALIAS_PER_CONTRATANTE + ".NOM_PRIMER_APELLIDO, ' ', " + ALIAS_PER_CONTRATANTE + ".NOM_SEGUNDO_APELLIDO) as nombreContratante",
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
                        "inventario.NOM_ARTICULO as nombreArticulo",
                        "dvs.CAN_ARTICULOS as cantidadArticulos",
                        "dvs.DES_OBSERVACION as observaciones")
                .from("SVT_VALE_SALIDA vs")
                .join(SVC_VELATORIO + " v", "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join(SVC_DELEGACION + " d", "d.ID_DELEGACION = :idDelegacion")
                .join(SVC_ORDEN_SERVICIO + " " + ALIAS_ODS,
                        "vs." + ID_ORDEN_SERVICIO + " = " + ALIAS_ODS + "." + ID_ORDEN_SERVICIO,
                        ALIAS_ODS + ".ID_ESTATUS_ORDEN_SERVICIO = 2")
                .join("SVC_FINADO " + ALIAS_USU_FINADO,
                        "v." + ID_VELATORIO + " = " + ALIAS_USU_FINADO + "." + ID_VELATORIO,
                        ALIAS_USU_FINADO + "." + ID_ORDEN_SERVICIO + " = " + ALIAS_ODS + "." + ID_ORDEN_SERVICIO)
                .join("SVC_PERSONA perFinado",
                        ALIAS_USU_FINADO + "." + ID_PERSONA + " = perFinado." + ID_PERSONA)
                .join(SVC_CONTRATANTE + " " + ALIAS_USU_CONTRATANTE,
                        ALIAS_ODS + "." + ID_CONTRATANTE + " = " + ALIAS_USU_CONTRATANTE + "." + ID_CONTRATANTE)
                .join(SVC_PERSONA + " " + ALIAS_PER_CONTRATANTE,
                        ALIAS_USU_CONTRATANTE + "." + ID_PERSONA + " = " + ALIAS_PER_CONTRATANTE + "." + ID_PERSONA)
                .join(SVC_INFORMACION_SERVICIO + " infoServ",
                        "infoServ." + ID_ORDEN_SERVICIO + " = " + ALIAS_ODS + "." + ID_ORDEN_SERVICIO)
                .join(SVC_INFORMACION_SERVICIO_VELACION + " infoOds",
                        "infoOds.ID_INFORMACION_SERVICIO = infoServ.ID_INFORMACION_SERVICIO")
                .join(SVT_DOMICILIO + " domicilio",
                        "domicilio.ID_DOMICILIO = infoOds.ID_DOMICILIO")
                .join(SVC_CP + " cp",
                        "cp.CVE_CODIGO_POSTAL = domicilio.DES_CP")
                .leftJoin("SVT_VALE_SALIDADETALLE dvs",
                        "dvs." + ID_VALE_SALIDA + " = vs." + ID_VALE_SALIDA, "dvs." + ID_ESTATUS + " = " + ESTATUS_SALIDA)
                .or("dvs." + ID_ESTATUS + " = " + ESTATUS_ENTRADA)
                .join("SVT_INVENTARIO inventario",
                        "dvs.ID_INVENTARIO = inventario.ID_INVENTARIO");

        queryUtil.where("vs." + ID_VALE_SALIDA + " = :idValeSalida",
                        "vs." + ID_ESTATUS + " <> " + ESTATUS_ELIMINADA)
                .setParameter(PARAM_ID_VALE_SALIDA, id)
                .setParameter(PARAM_ID_DELEGACION, idDelegacion)
                .groupBy("dvs.ID_INVENTARIO");

        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, encoded);
        DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);


        return datos;
    }

    /**
     * Recupera el nombre completo de una persona
     *
     * @param referencia
     * @param alias
     * @return
     */
    private static String recuperaNombre(String referencia, String alias) {
        return "CONCAT(" + referencia + "." + NOM_PERSONA + ", ' ', " + referencia + "." + NOM_PRIMER_APELLIDO + ", ' ', " + referencia + "." + NOM_SEGUNDO_APELLIDO + ") as " + alias;
    }

    /**
     * Elimina los productos de un vale de salida
     *
     * @param idValeSalida
     * @return
     */
    public DatosRequest cambiarEstatusDetalleValeSalida(Long idValeSalida, Integer idUsuario, int estatus) {

        StringBuilder queryBuilder = new StringBuilder("UPDATE SVT_VALE_SALIDADETALLE SET ");
        queryBuilder.append(ID_ESTATUS).append(" = ").append(estatus).append(", ")
                .append("ID_USUARIO_MODIFICA = ").append(idUsuario).append(", ")
                .append("FEC_ACTUALIZACION = ").append(CURRENT_TIMESTAMP).append(" ")
                .append("WHERE ")
                .append(ID_VALE_SALIDA).append(" ").append(idValeSalida)
                .append(" AND ")
                .append(ID_ESTATUS).append(" = ").append(ESTATUS_SALIDA);

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
     * @param filtros
     * @return
     */
    public DatosRequest consultarValesSalida(DatosRequest request, FiltrosRequest filtros) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();

        queryUtil.select(ID_VALE_SALIDA + " as idValeSalida",
                        "v." + ID_VELATORIO + " as idVelatorio",
                        "v." + NOM_VELATORIO + " as nombreVelatorio",
                        "vs.ID_ORDEN_SERVICIO as idOds",
                        ALIAS_ODS + ".CVE_FOLIO as folioOds",
                        "vs.FEC_SALIDA as fechaSalida",
                        "vs.FEC_ENTRADA as fechaEntrada",
                        "vs." + ID_ESTATUS + " as idEstatus",
                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
                        "vs.NOM_RESPON_INSTA as nombreResponsableInstalacion",
                        "vs.CAN_ARTICULOS as totalArticulos",
//                        "CONCAT(" + ALIAS_PER_CONTRATANTE + ".NOM_PERSONA, ' ', " + ALIAS_PER_CONTRATANTE + ".NOM_PRIMER_APELLIDO, ' ', " + ALIAS_PER_CONTRATANTE + ".NOM_SEGUNDO_APELLIDO) as nombreContratante")
                        recuperaNombre(ALIAS_PER_CONTRATANTE, ALIAS_NOMBRE_CONTRATANTE))
                .from("SVT_VALE_SALIDA vs")
                .join("SVC_VELATORIO v",
                        "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join(SVC_ORDEN_SERVICIO + " " + ALIAS_ODS,
                        "vs.ID_ORDEN_SERVICIO = " + ALIAS_ODS + ".ID_ORDEN_SERVICIO")
                .join(SVC_CONTRATANTE + " " + ALIAS_USU_CONTRATANTE,
                        ALIAS_ODS + ".ID_CONTRATANTE = usuContratante.ID_CONTRATANTE")
                .join(SVC_PERSONA + " " + ALIAS_PER_CONTRATANTE,
                        "usuContratante." + ID_PERSONA + " = " + ALIAS_PER_CONTRATANTE + "." + ID_PERSONA)
                .where("vs." + ID_ESTATUS + " <> " + ESTATUS_ELIMINADA);

        if (filtros != null && !filtros.validarNulos()) {
            if (filtros.getIdVelatorio() != null) {
                queryUtil.where("vs." + ID_VELATORIO + " = :idVelatorio")
                        .setParameter(PARAM_ID_VELATORIO, filtros.getIdVelatorio());
            }
            if (filtros.getFolioOds() != null) {
                queryUtil.where("ods." + FOLIO_ODS + " = :folioOds")
                        .setParameter(PARAM_FOLIO_ODS, filtros.getFolioOds());
            }
            if (filtros.getFechaInicio() != null && filtros.getFechaFinal() != null) {
                if (filtros.validarFechas()) {
                    queryUtil.where("vs.FEC_SALIDA >= :fechaInicial",
                                    "vs.FEC_SALIDA <= :fechaFin")
                            .setParameter("fechaInicial", filtros.getFechaInicio())
                            .setParameter("fechaFin", filtros.getFechaFinal());
                } else {
                    throw new ValidacionFechasException(HttpStatus.BAD_REQUEST, "Rango inválido de fechas.");
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
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select(
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "d.DES_DELEGACION as nombreDelegacion",
                        "ods.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as request",
//                        "CONCAT(" + ALIAS_PER_CONTRATANTE + ".NOM_PERSONA, ' ', " + ALIAS_PER_CONTRATANTE + ".NOM_PRIMER_APELLIDO, ' ', " + ALIAS_PER_CONTRATANTE + ".NOM_SEGUNDO_APELLIDO) as nombreContratante",
                        recuperaNombre(ALIAS_PER_CONTRATANTE, ALIAS_NOMBRE_CONTRATANTE),
//                        "CONCAT(perFinado.NOM_PERSONA, ' ', perFinado.NOM_PRIMER_APELLIDO, ' ', perFinado.NOM_SEGUNDO_APELLIDO) as nombreFinado",
                        recuperaNombre(ALIAS_PER_FINADO, ALIAS_NOMBRE_FINADO),
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
                .join("SVC_DELEGACION d", "d.ID_DELEGACION = v.ID_DELEGACION")
                .join("SVC_FINADO " + ALIAS_USU_FINADO,
                        "v.ID_VELATORIO = " + ALIAS_USU_FINADO + ".ID_VELATORIO",
                        ALIAS_USU_FINADO + ".ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join(SVC_PERSONA + " perFinado",
                        ALIAS_USU_FINADO + "." + ID_PERSONA + " = perFinado." + ID_PERSONA)
                .join(SVC_CONTRATANTE + " usuContratante",
                        "ods." + ID_CONTRATANTE + " = usuContratante." + ID_CONTRATANTE)
                .join(SVC_PERSONA + " " + ALIAS_PER_CONTRATANTE,
                        "usuContratante.ID_PERSONA = " + ALIAS_PER_CONTRATANTE + ".ID_PERSONA")
                .join("SVC_INFORMACION_SERVICIO infoServ", "infoServ.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_INFORMACION_SERVICIO_VELACION infoOds",
                        "infoOds.ID_INFORMACION_SERVICIO = infoServ.ID_INFORMACION_SERVICIO")
                .join("SVT_DOMICILIO domicilio", "domicilio.ID_DOMICILIO = infoOds.ID_DOMICILIO")
                .join("SVC_CP cp", "cp.CVE_CODIGO_POSTAL = domicilio.DES_CP")
                .join("SVT_INVENTARIO inventario",
                        "inventario.ID_VELATORIO = v.ID_VELATORIO",
                        "inventario.ID_TIPO_SERVICIO = " + TIPO_SERVICIO_RENTA_EQUIPO);

        queryUtil.where(
                        "ods.CVE_FOLIO = :folioOds",
                        "ods.ID_ESTATUS_ORDEN_SERVICIO = " + ESTATUS_ODS_GENERADA,
                        "d.ID_DELEGACION = :idDelegacion",
                        "v.ID_VELATORIO = :idVelatorio")
                .setParameter(PARAM_FOLIO_ODS, request.getFolioOds())
                .setParameter(PARAM_ID_DELEGACION, request.getIdDelegacion())
                .setParameter(PARAM_ID_VELATORIO, request.getIdVelatorio());

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
        final Integer idUsuario = usuarioDto.getIdUsuario();
        final List<DetalleValeSalidaRequest> articulos = valeSalida.getArticulos();

        QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDA");
        queryHelper.agregarParametroValues(ID_VELATORIO, String.valueOf(valeSalida.getIdVelatorio()));
        queryHelper.agregarParametroValues(ID_ORDEN_SERVICIO, String.valueOf(valeSalida.getIdOds()));
        queryHelper.agregarParametroValues("FEC_SALIDA", "STR_TO_DATE('" + valeSalida.getFechaSalida() + "', '%d-%m-%Y')");
        queryHelper.agregarParametroValues("NOM_RESPON_ENTREGA", "'" + String.valueOf(valeSalida.getNombreResponsableEntrega()) + "'");
        queryHelper.agregarParametroValues("NOM_RESPON_INSTA", "'" + valeSalida.getNombreResponsableInstalacion() + "'");
        queryHelper.agregarParametroValues("CVE_MATRICULA_RESINST", "'" + valeSalida.getMatriculaResponsableInstalacion() + "'");

        queryHelper.agregarParametroValues("NUM_DIA_NOVENARIO", String.valueOf(valeSalida.getDiasNovenario()));
        queryHelper.agregarParametroValues(CAN_ARTICULOS, String.valueOf(valeSalida.getCantidadArticulos()));

        queryHelper.agregarParametroValues(ID_ESTATUS, String.valueOf(ESTATUS_SALIDA));

        queryHelper.agregarParametroValues(ID_USUARIO_ALTA, String.valueOf(idUsuario));
        queryHelper.agregarParametroValues(FEC_ALTA, CURRENT_TIMESTAMP);

        String queriesArticulos = crearDetalleVale(articulos, idUsuario);
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
    private String crearDetalleVale(List<DetalleValeSalidaRequest> articulos, Integer idUsuario) {
        StringBuilder query = new StringBuilder();
        for (DetalleValeSalidaRequest detalleValeSalida : articulos) {
            QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDADETALLE");
            queryHelper.agregarParametroValues(ID_VALE_SALIDA, "idTabla");
            queryHelper.agregarParametroValues("ID_INVENTARIO", String.valueOf(detalleValeSalida.getIdInventario()));
            queryHelper.agregarParametroValues(CAN_ARTICULOS, String.valueOf(detalleValeSalida.getCantidad()));
            queryHelper.agregarParametroValues("DES_OBSERVACION", "'" + detalleValeSalida.getObservaciones() + "'");

            queryHelper.agregarParametroValues(ID_USUARIO_ALTA, String.valueOf(idUsuario));
            queryHelper.agregarParametroValues(FEC_ALTA, CURRENT_TIMESTAMP);

            queryHelper.agregarParametroValues(ID_ESTATUS, String.valueOf(ESTATUS_SALIDA));
            query.append(" $$ ").append(queryHelper.obtenerQueryInsertar());
        }
        return query.toString();
    }

    /**
     * www
     *
     * @param queries
     * @return
     */
    public List<String> generarQueries(String... queries) {

        Map<String, Object> parametros = new HashMap<>();
        final List<String> updates = Arrays.asList(queries);
        parametros.put("updates", updates.stream().map(ValeSalida::getBinary));

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
                    .append(ID_ESTATUS + " = ").append(ESTATUS_ENTRADA).append(", ")
                    .append("NOM_RESPEQUIVELACION = ").append("'").append(valeSalida.getNombreResponsableEntrega()).append("', ")
                    .append("CVE_MATRICULARESPEQUIVELACION = ").append("'").append(valeSalida.getMatriculaResponsableEntrega()).append("' ");
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
        queryHelper.agregarParametroValues(ID_VALE_SALIDA, String.valueOf(idValeSalida));
        queryHelper.agregarParametroValues("ID_INVENTARIO", String.valueOf(articulo.getIdInventario()));
        queryHelper.agregarParametroValues(CAN_ARTICULOS, String.valueOf(articulo.getCantidad()));
        queryHelper.agregarParametroValues("DES_OBSERVACION", "'" + articulo.getObservaciones() + "'");
        queryHelper.agregarParametroValues(ID_ESTATUS, String.valueOf(estatus));

        queryHelper.agregarParametroValues(ID_USUARIO_ALTA, String.valueOf(idUsuario));
        queryHelper.agregarParametroValues(FEC_ALTA, CURRENT_TIMESTAMP);

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
    public DatosRequest consultarFoliosOds(ConsultaFoliosRequest request) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("ods.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as folioOds")
                .from("SVC_ORDEN_SERVICIO ods")
                .join("SVC_VELATORIO velatorio", "velatorio.ID_VELATORIO = ods.ID_VELATORIO")
                .join("SVC_DELEGACION delegacion", "delegacion.ID_DELEGACION = velatorio.ID_DELEGACION")
                .where("ID_ESTATUS_ORDEN_SERVICIO = 2");
        if (request.getIdDelegacion() != null) {
            queryUtil.and("delegacion.ID_DELEGACION = :idDelegacion")
                    .setParameter(PARAM_ID_DELEGACION, request.getIdDelegacion());
        }
        if (request.getIdVelatorio() != null) {
            queryUtil.and("velatorio.ID_VELATORIO = :idVelatorio")
                    .setParameter(PARAM_ID_VELATORIO, request.getIdVelatorio());
        }
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
        parametros.put(PARAM_FOLIO_ODS, reporteDto.getFolioOds());
        parametros.put("fechaSalida", reporteDto.getFechaSalida());
        parametros.put("domicilio", reporteDto.getDomicilio());
        parametros.put("ciudad", reporteDto.getEstado());
        parametros.put("nombreResponsableInstalacion", reporteDto.getNombreResponsableInstalacion());
        parametros.put("matriculaResponsableInstalacion", reporteDto.getMatriculaResponsableInstalacion());
        parametros.put("condition", "WHERE vsd.`ID_VALESALIDA` = " + reporteDto.getIdValeSalida() + " AND vsd." + ID_ESTATUS + " = 2 OR vsd." + ID_ESTATUS + " = 1");
        parametros.put("nombreResponsableEquipo", reporteDto.getNombreResponsableEquipo());
        parametros.put("matriculaResponsableEquipo", reporteDto.getMatriculaResponsableEquipo());

        parametros.put(ALIAS_NOMBRE_CONTRATANTE, reporteDto.getNombreContratante());

        if (reporteDto.getFechaEntrega() != null && !reporteDto.getFechaEntrega().isEmpty()) {
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

        }

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
        parametros.put("rutaNombreReporte", filtros.getRuta());
        parametros.put("tipoReporte", filtros.getTipoReporte());
        parametros.put("idValeSalida", filtros.getIdValeSalida());
        parametros.put(PARAM_ID_VELATORIO, filtros.getIdVelatorio());
        parametros.put("velatorio", filtros.getNombreVelatorio());
        parametros.put(PARAM_FOLIO_ODS, filtros.getFolioOds());
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
        queryBuilder.append("SET ")
                .append(ID_ESTATUS).append(" = ").append(ESTATUS_ELIMINADA).append(", ")
                .append("ID_USUARIO_ACTUALIZA").append(" = ").append(idUsuario).append(", ")
                .append("FEC_ACTUALIZACION").append(" = ").append(CURRENT_TIMESTAMP)
                .append(" WHERE ")
                .append(ID_VALE_SALIDA).append(" = ").append(idValeSalida);

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
