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
import java.text.DateFormat;
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

    private final Gson gson;

    public ValeSalida() {
        this.gson = new Gson();
    }

    // consultar vale de salida (detalle)
    //   - hay que recuperar tambien la lista de articulos del inventario del velatorio
    //   - revisar si esta funcion cubre la consulta del reporte
    public DatosRequest consultar(long id) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("vs.ID_VALESALIDA as idValeSalida",
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "vs.CVE_FOLIO as folioValeSalida",
                        "vs.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as folioOds",
                        "perContratante.NOM_PERSONA as nombreContratante",
                        "perFinado.NOM_PERSONA as nombreFinado",
                        "vs.FEC_SALIDA as fechaSalida",
                        "vs.FEC_ENTRADA as fechaEntrada",
                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
                        "vs.NOM_RESPON_INSTA as nombreResponsableInstalacion",
                        "vs.CVE_MATRICULA_RESPON as matriculaResponsableInstalacion",
                        "vs.NOM_RESPEQUIVELACION as nombreResponsableEquipo",
                        "vs.CVE_MATRICULARESPEQUIVELACION as matriculaResponsableEquipo",
                        "vs.CAN_ARTICULOS as totalArticulos",
                        "infoOds.DES_CALLE as calle",
                        "infoOds.NUM_EXTERIOR as numExt",
                        "infoOds.NUM_INTERIOR as numInt",
                        "infoOds.DES_COLONIA as colonia",
                        "cp.DES_ESTADO as estado",
                        "cp.DES_MNPIO as municipio",
                        "cp.CVE_CODIGO_POSTAL as codigoPostal",
                        "dvs.ID_ARTICULO as idArticulo",
                        "dvs.CAN_ARTICULOS as cantidadArticulos",
                        "dvs.DES_OBSERVACION as observaciones")
                .from("SVT_VALE_SALIDA vs")
                // todo - pasar como parametro tambien el idDelegacion
                .join("SVC_VELATORIO v", "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join("SVC_ORDEN_SERVICIO ods", "vs.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_FINADO usuFinado", "v.ID_VELATORIO = usuFinado.ID_VELATORIO", "usuFinado.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_PERSONA perFinado", "usuFinado.ID_PERSONA = perFinado.ID_PERSONA")
                .join("SVC_CONTRATANTE usuContratante", "ods.ID_CONTRATANTE = usuContratante.ID_CONTRATANTE")
                .join("SVC_PERSONA perContratante", "usuContratante.ID_PERSONA = perContratante.ID_PERSONA")
                .join("SVC_INFORMACION_SERVICIO infoServ", "infoServ.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_INFORMACION_SERVICIO_VELACION infoOds", "infoOds.ID_INFORMACION_SERVICIO = infoServ.ID_INFORMACION_SERVICIO")
                .join("SVC_CP cp", "cp.ID_CODIGO_POSTAL = infoOds.ID_CP")
                .leftJoin("SVT_VALE_SALIDADETALLE dvs", "dvs.ID_VALESALIDA = vs.ID_VALESALIDA");
        queryUtil.where("vs.ID_VALESALIDA = :idValeSalida")
                .setParameter("idValeSalida", id);
        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, encoded);
        DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);


        return datos;
    }

    /**
     * todo - agregar documentacion
     * todo - remover no se esta usando
     *
     * @param idVelatorio
     * @return
     */
    public DatosRequest consultarProductos(long idVelatorio) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        // todo - movel el tipo de servicio a una constante tipo de servicio 2 - Renta de equipo
        queryUtil.select("ID_ARTICULO as idArticulo",
                        "a.DES_ARTICULO as nombreArticulo",
                        "inventario.CAN_STOCK as cantidad")
                .from("SVT_INVENTARIO inventario")
                .join("SVT_ARTICULO a", "a.ID_ARTICULO = inventario.ID_ARTICULO")
                .where("inventario.ID_TIPO_SERVICIO = :idTipoServicio",
                        "inventario.ID_VELATORIO = :idVelatorio")
                .setParameter("idTipoServicio", 2)
                .setParameter("idVelatorio", idVelatorio);
        String query = getQuery(queryUtil);
        Map<String, Object> parametros = new HashMap<String, Object>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
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
    public DatosRequest eliminarProductosVale(Long idValeSalida) {
        // todo - no hay delete, por lo que se tienen que hacer un borrado logico con el campo cve_estatus
        String query = "UPDATE SVT_VALE_SALIDADETALLE set cve_estatus = false where ID_VALESALIDA = " + idValeSalida;

        Map<String, Object> parametros = new HashMap<>();
        DatosRequest datos = new DatosRequest();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        datos.setDatos(parametros);

        return datos;
    }

    // consultar vales de salid

    /**
     * todo - probar que funcione correctamente
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
//                        "vs.FEC_ENTRADA as fechaEntrada",
                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
                        "NOM_RESPON_INSTA as nombreResponsableInstalacion",
                        "CVE_MATRICULA_RESPON as matriculaResponsableInstalacion",
                        "NOM_RESPEQUIVELACION as nombreResponsableEquipo",
                        "CVE_MATRICULARESPEQUIVELACION as matriculaResponsableEquipo")
                .from("SVT_VALE_SALIDA vs")
                .join("SVC_VELATORIO v", "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join("SVC_ORDEN_SERVICIO ods", "vs.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO");

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
                    queryUtil.where("vs.FECHA_SALIDA >= :fechaInicial",
                                    "vs.FECHA_SALIDA <= :fechaFin")
                            .setParameter("fechaInicial", filtros.getFechaInicio())
                            .setParameter("fechaFin", filtros.getFechaFinal());
                }
            }
        }
        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
//        Map<String, Object> parametros = new HashMap<>();
//        parametros.put(AppConstantes.QUERY, encoded);

        request.getDatos().put(AppConstantes.QUERY, encoded);
        request.getDatos().remove(AppConstantes.DATOS);
        return request;
    }

    // consultar datos de la ods nombre contratante, nombre responsable y nombre finado

    /**
     * Consulta los datos de la ODS, consulta tambi&eacute;n la lista de art&iacute;culos disponibles para ese
     * velatorio.
     *
     * @param request
     * @return
     */
    public DatosRequest consultarDatosOds(ConsultaDatosPantallaRequest request) {
        // todo - recuperar tanto el request como el idVelatorio

        // todo - validar que los campos no sean nulos
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "d.DES_DELEGACION as nombreDelegacion",
//                        "vs.CVE_FOLIO as folioValeSalida",
                        "ods.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as request",
                        "perContratante.NOM_PERSONA as nombreContratante",
                        "perFinado.NOM_PERSONA as nombreFinado",
//                        "vs.FEC_SALIDA as fechaSalida",
//                        "vs.FEC_ENTRADA as fechaEntrada",
//                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
//                        "vs.NOM_RESPON_INSTA as nombreResponsableInstalacion",
//                        "vs.CVE_MATRICULA_RESPON as matriculaResponsableInstalacion",
//                        "vs.NOM_RESPEQUIVELACION as nombreResponsableEquipo",
//                        "vs.CVE_MATRICULARESPEQUIVELACION as matriculaResponsableEquipo",
//                        "vs.CAN_ARTICULOS as totalArticulos",
                        "infoOds.DES_CALLE as calle",
                        "infoOds.NUM_EXTERIOR as numExt",
                        "infoOds.NUM_INTERIOR as numInt",
                        "infoOds.DES_COLONIA as colonia",
                        "cp.DES_ESTADO as estado",
                        "cp.DES_MNPIO as municipio",
                        "cp.CVE_CODIGO_POSTAL as codigoPostal",
                        "inventario.ID_INVENTARIO as idArticulo",
                        "inventario.CAN_STOCK as cantidadArticulos")
                .from("SVC_ORDEN_SERVICIO ods")
                .join("SVC_VELATORIO v", "v.ID_VELATORIO = :idVelatorio")
                .join("SVC_DELEGACION d", "d.id_delegacion = v.id_delegacion")
                .join("SVC_FINADO usuFinado", "v.ID_VELATORIO = usuFinado.ID_VELATORIO", "usuFinado.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_PERSONA perFinado", "usuFinado.ID_PERSONA = perFinado.ID_PERSONA")
                .join("SVC_CONTRATANTE usuContratante", "ods.ID_CONTRATANTE = usuContratante.ID_CONTRATANTE")
                .join("SVC_PERSONA perContratante", "usuContratante.ID_PERSONA = perContratante.ID_PERSONA")
                .join("SVC_INFORMACION_SERVICIO infoServ", "infoServ.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_INFORMACION_SERVICIO_VELACION infoOds", "infoOds.ID_INFORMACION_SERVICIO = infoServ.ID_INFORMACION_SERVICIO")
                .join("SVC_CP cp", "cp.ID_CODIGO_POSTAL = infoOds.ID_CP")
                .leftJoin("SVT_INVENTARIO inventario", "inventario.ID_VELATORIO = v.ID_VELATORIO");

        queryUtil.where("ods.CVE_FOLIO = :folioOds")
                .setParameter("folioOds", request.getFolioOds())
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

    // crear vale de salida
    //   - crear el registro en la tabla vale de salida
    //   - hay que crear tambien un registro por cada articulo que se agregue en el registro
    //   - hay que descontar la cantidad de articulos del inventario

    /**
     * Craa un registro para el vale de salida con su respectivo detalle
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
//        queryHelper.agregarParametroValues("CVE_FOLIO", valeSalida.getFolioValeSalida());
        queryHelper.agregarParametroValues("ID_ORDEN_SERVICIO", String.valueOf(valeSalida.getIdOds()));
        queryHelper.agregarParametroValues("FEC_SALIDA", "STR_TO_DATE('" + String.valueOf(valeSalida.getFechaSalida()) + "', '%d-%m-%Y')");
//        queryHelper.agregarParametroValues("FEC_SALIDA", String.valueOf(valeSalida.getFechaSalida()));
//        queryHelper.agregarParametroValues("NOM_RESPON_ENTREGA", String.valueOf(valeSalida.getNombreResponsableEntrega()));
//        queryHelper.agregarParametroValues("CVE_MATRICULA_RESPON", String.valueOf(valeSalida.getMatriculaResponsableEntrega()));
        queryHelper.agregarParametroValues("NOM_RESPON_INSTA", "'" + String.valueOf(valeSalida.getNombreResponsableInstalacion()) + "'");
        // todo - falta la matricula del responsable de la instalacion
        queryHelper.agregarParametroValues("NOM_RESPEQUIVELACION", "'" + String.valueOf(valeSalida.getNombreResponsableEquipoVelacion()) + "'");
        queryHelper.agregarParametroValues("CVE_MATRICULARESPEQUIVELACION", "'" + String.valueOf(valeSalida.getMatriculaResponsableEquipoVelacion()) + "'");

        queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(valeSalida.getCantidadArticulos()));
        queryHelper.agregarParametroValues("CVE_ESTATUS", "1");

        queryHelper.agregarParametroValues("ID_USUARIO_ALTA", String.valueOf(usuarioDto.getIdUsuario()));
        queryHelper.agregarParametroValues("FEC_ALTA", "CURRENT_TIMESTAMP");

        final List<DetalleValeSalidaRequest> articulos = valeSalida.getArticulos();
        // todo - mover esta validacon al servicio
        if (articulos.isEmpty()) {
            // todo - recuperar el mensaje del catalogo
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "La lista de articulos no puede estar vacia");
        }
        String queriesArticulos = crearDetalleVale(articulos);
        String query = queryHelper.obtenerQueryInsertar() + queriesArticulos;
        parametros.put(AppConstantes.QUERY, getBinary(query));
        parametros.put("separador", "$$");
        parametros.put("replace", "idTabla");
        datos.setDatos(parametros);
        return datos;
    }

    /**
     * Crea los resgistros del detalle del Vale de salida, para que se pueda hacer la relaci&oacute;n entre
     * Vale de Salida y su Detalle.
     *
     * @param articulos
     * @return
     */
    private String crearDetalleVale(List<DetalleValeSalidaRequest> articulos) {
        StringBuilder query = new StringBuilder();
        for (DetalleValeSalidaRequest detalleValeSalida : articulos) {
            QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDADETALLE");
            queryHelper.agregarParametroValues("ID_VALESALIDA", "idTabla");
            queryHelper.agregarParametroValues("ID_ARTICULO", String.valueOf(detalleValeSalida.getIdInventario()));
            queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(detalleValeSalida.getCantidad()));
            queryHelper.agregarParametroValues("DES_OBSERVACION", "'" + detalleValeSalida.getObservaciones() + "'");
            query.append(" $$ ").append(queryHelper.obtenerQueryInsertar());
        }
        return query.toString();
    }

    // modificar vale de salida
    //   - hay que modificar tambien el registro de cada articulo que se modifique en el registro
    public DatosRequest modificarVale(ValeSalidaDto valeSalida, boolean registrarEntrada) {
        // todo - agregar solo los campos que se puedan modificar
        // que es lo que se va a modificar en esta parte?
        //
        QueryHelper queryHelper = new QueryHelper("UPDATE SVT_VALE_SALIDA");
        queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(valeSalida.getCantidadArticulos()));
        // todo - hacer la logica para la entrada de articulos
        // se registran nombres de los responsables
        // todo - recuperar los nombres de los responsables y la matricula
        if (registrarEntrada) {
            queryHelper.agregarParametroValues("FEC_SALIDA", "STR_TO_DATE('" + String.valueOf(valeSalida.getFechaSalida()) + "', '%d-%m-%Y')");
            // todo - verificar que los nombres y matricula sean correctos
            // verificar que solo sean esos datos los que se van a estar guardando
            queryHelper.agregarParametroValues("nom_responsable", "'" + String.valueOf(valeSalida.getCantidadArticulos()) + "'");
            queryHelper.agregarParametroValues("cve_matricula_responsable", "'" + valeSalida.getCantidadArticulos() + "'");
        }

        String query = queryHelper.obtenerQueryActualizar();
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
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

        final String query = updateQuery.toString();
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        datos.setDatos(parametros);
        return datos;
    }

    /**
     * todo - agregar documentacion
     *
     * @return
     */
    public DatosRequest consultarFoliosOds() {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("ID_ORDEN_SERVICIO as idOds",
                        "CVE_FOLIO as folioOds")
                .from("SVC_ORDEN_SERVICIO")
                .where("CVE_ESTATUS = 1");
        return getDatosRequest(queryUtil);
    }

    public void consultarValeSalida() {

    }

    public Map<String, Object> generarReporte(ReporteDto reporteDto) throws ParseException {

        // todo lo que se puede hacer es hacer la consulta y con los datos que me regresa la consulta, armar el objeto que va a usar el formato
        // hay que revisar como pasar una lista de objetos al jasper
        // todo - hay que pasar todos los parametros al map

//        String fechaCompleta = reporteDto.getMes() + "-" + reporteDto.getAnio();
//        Date dateF = new SimpleDateFormat("MMMM-yyyy").parse(fechaCompleta);
//        DateFormat anioMes = new SimpleDateFormat("yyyy-MM", new Locale("es", "MX"));
//        String fecha = anioMes.format(dateF);
//        log.info("estoy en:" + fecha);
        Map<String, Object> parametros = new HashMap<>();
        // pasar los datos que faltan para el formato
        parametros.put("logoImss", "");
        parametros.put("logoSistema", "");
        // todo - ver que se le pasa la condicion o si ya no es necesario pasar este parametro
//        private Long idOoad;
        // todo - recuperar el nombre de la delegacion
//        parametros.put("idOoad", reporteDto.getIdOoad());
//        parametros.put("idVelatorio", reporteDto.getIdVelatorio());
        parametros.put("nombreDelegacion", reporteDto.getNombreDelegacion());
//        private Long idVelatorio;
        parametros.put("nombreVelatorio", reporteDto.getNombreVelatorio());
//        private String nombreResponsableEntrega;
        parametros.put("nombreResponsableEntrega", reporteDto.getNombreResponsableEntrega());
//        private Integer diasNovenario;
        parametros.put("diasNovenario", reporteDto.getDiasNovenario());
//        private String folioOds;
        parametros.put("folioOds", reporteDto.getFolioOds());
//        private String fechaActual;
//        parametros.put("fechaActual", )
//        private String domicilio;
        parametros.put("domicilio", reporteDto.getDomicilio());
//        private String nombreResponsableInstalacion;
        parametros.put("nombreResponsableInstalacion", reporteDto.getNombreResponsableInstalacion());
//        private List<DetalleValeSalidaRequest> articulos;
        parametros.put("articulos", reporteDto.getArticulos());
//        private String nombreResponsableEquipo;
        parametros.put("nombreResponsableEquipo", reporteDto.getNombreResponsableEquipo());
//        private String fechaEntrega;
        parametros.put("fechaEntrega", reporteDto.getFechaEntrega());
//        private String fechaSalida;
        parametros.put("fechaSalida", reporteDto.getFechaSalida());
//        parametros.put("condition", " AND SDC.FEC_ENTRADA LIKE '%" + fecha + "%' AND SV.NOM_VELATORIO = '" + reporteDto.getVelatorio() + "'");
        parametros.put("rutaNombreReporte", reporteDto.getRuta());
        parametros.put("tipoReporte", reporteDto.getTipoReporte());

        return parametros;
    }

    // generar el formato, ver si se usa la consulta del detalle del vale de salida

    private static String getBinary(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes());
    }

    private static String getQuery(SelectQueryUtil queryUtil) {
        return queryUtil.build();
    }

}
