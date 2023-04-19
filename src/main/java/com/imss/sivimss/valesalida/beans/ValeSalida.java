package com.imss.sivimss.valesalida.beans;

import com.imss.sivimss.valesalida.model.request.DetalleValeSalidaRequest;
import com.imss.sivimss.valesalida.model.request.FiltrosRequest;
import com.imss.sivimss.valesalida.model.request.UsuarioDto;
import com.imss.sivimss.valesalida.model.request.ValeSalidaRequest;
import com.imss.sivimss.valesalida.model.response.ValeSalidaDto;
import com.imss.sivimss.valesalida.util.AppConstantes;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.QueryHelper;
import com.imss.sivimss.valesalida.util.SelectQueryUtil;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase para controlar la l&oacute;gica de negocio para administrar los movimientos de art&iacute;culos
 * asignados a una
 */
@Component
public class ValeSalida {

    // consultar vale de salida (detalle)
    //   - hay que recuperar tambien la lista de articulos del inventario del velatorio
    //   - revisar si esta funcion cubre la consulta del reporte
    public DatosRequest consultar(long id) {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("ID_VALESALIDA as idValeSalida",
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "vs.CVE_FOLIO as folioValeSalida",
                        "vs.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as folioOds",
                        "usuContratante.NOM_USUARIO as nombreContratante",
                        "usuFinado.NOM_USUARIO as nombreFinado",
                        "vs.FEC_SALIDA as fechaSalida",
                        "vs.FEC_ENTRADA as fechaEntrada",
                        "vs.NUM_DIA_NOVENARIO as diasNovenario",
                        "NOM_RESPON_INSTA as nombreResponsableInstalacion",
                        "CVE_MATRICULA_RESPON as matriculaResponsableInstalacion",
                        "NOM_RESPEQUIVELACION as nombreResponsableEquipo",
                        "CVE_MATRICULARESPEQUIVELACION as matriculaResponsableEquipo",
                        "infoOds.DES_CALLE as calle",
                        "infoOds.NUM_EXTERIOR as numExt",
                        "infoOds.NUM_INTERIOR as numInt",
                        "infoOds.DES_COLONIA as colonia",
                        "cp.DES_ESTADO as estado",
                        "cp.DES_MUNICIPIO as municipio",
                        "cp.CVE_CODIGO_POSTAL as codigoPostal",
                        "CAN_ARTICULOS as totalArticulos",
                        "dvs.ID_ARTICULO as idArticulo",
                        "dvs.CAN_ARTICULOS as cantidadArticulos",
                        "dvs.DES_OBSERVACION as observaciones")
                .from("SVT_VALE_SALIDA vs")
                .join("SVC_VELATORIO v", "vs.ID_VELATORIO = v.ID_VELATORIO")
                .join("SVC_ORDEN_SERVICIO ods", "vs.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVC_FINADO usuFinado", "v.ID_VELATORIO = usuFinado.ID_VELATORIO", "usuFinado.ID_ORDEN_SERVICIO = ods.ID_ORDEN_SERVICIO")
                .join("SVT_USUARIOS usuContratante", "ods.ID_CONTRATANTE = usuContratante.ID_USUARIO")
//                .join("svt_usuarios usuarioReg", "usuarioReg.id")
                .leftJoin("SVT_VALE_SALIDADETALLE dvs", "dvs.ID_VALESALIDA = vs.ID_VALESALIDA");
//                if (filtros.)
        queryUtil.where("vs.id_valesalida = :idValeSalida")
                .setParameter("idValeSalida", id);
        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, encoded);
        DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);


        return datos;
    }

    // todo - este parece que no va

    /**
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

    private static String getBinary(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes());
    }

    private static String getQuery(SelectQueryUtil queryUtil) {
        return queryUtil.build();
    }

    /**
     * Elimina los productos de un vale de salida
     *
     * @param idValeSalida
     * @return
     */
    public String eliminarProductosVale(Long idValeSalida) {
        String query = "delete SVT_VALE_SALIDADETALLE where ID_VALESALIDA = " + idValeSalida;
        return "";
    }

    // consultar vales de salida
    //   - la consulta se hace por filtros, por lo que hay que hay que hacer ciertas validaciones del dto
    public DatosRequest consultarValesSalida(FiltrosRequest filtros) {
        final DatosRequest datos = new DatosRequest();
        SelectQueryUtil queryUtil = new SelectQueryUtil();

        queryUtil.select("ID_VALESALIDA as idValeSalida",
                        "v.ID_VELATORIO as idVelatorio",
                        "v.NOM_VELATORIO as nombreVelatorio",
                        "vs.ID_ORDEN_SERVICIO as idOds",
                        "ods.CVE_FOLIO as folioOds",
                        "vs.FEC_SALIDA as fechaSalida",
                        "vs.FEC_ENTRADA as fechaEntrada",
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
                queryUtil.where("vs.id_velatorio = :idVelatorio");

            }
            if (filtros.getFolioOds() != null) {

                queryUtil.where("ods.cve_folio = :folioOds");
            }
            if (filtros.getFechaInicio() != null && filtros.getFechaFinal() != null) {
                if (filtros.validarFechas()) {
                    queryUtil.where("vs.fechaSalida >= :fechaInicial",
                            "vs.fechaSalida <= :fechaFin");
                }
            }
        }
        Map<String, Object> parametros = new HashMap<>();
        String query = getQuery(queryUtil);
        final String encoded = getBinary(query);
        parametros.put(AppConstantes.QUERY, encoded);

        datos.setDatos(parametros);
        return datos;
    }

    // consultar datos de la ods nombre contratante, nombre responsable y nombre finado
    public String consultarDatosOds(String folioOds) {
        return "";
    }

    // crear vale de salida
    //   - crear el registro en la tabla vale de salida
    //   - hay que crear tambien un registro por cada articulo que se agregue en el registro
    //   - hay que descontar la cantidad de articulos del inventario
    public DatosRequest crearVale(ValeSalidaRequest valeSalida, UsuarioDto usuarioDto) {
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();
        QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDA");
        queryHelper.agregarParametroValues("ID_VELATORIO", String.valueOf(valeSalida.getIdVelatorio()));
        queryHelper.agregarParametroValues("CVE_FOLIO", valeSalida.getFolioValeSalida());
        queryHelper.agregarParametroValues("ID_ORDEN_SERVICIO", String.valueOf(valeSalida.getIdOds()));
        queryHelper.agregarParametroValues("NOM_RESPON_ENTREGA", String.valueOf(valeSalida.getNombreResponsableEntrega()));
        queryHelper.agregarParametroValues("FEC_SALIDA", String.valueOf(valeSalida.getFechaSalida()));
        queryHelper.agregarParametroValues("NOM_RESPON_INSTA", String.valueOf(valeSalida.getNombreResponsableInstalacion()));
        queryHelper.agregarParametroValues("CVE_MATRICULA_RESPON", String.valueOf(valeSalida.getMatriculaUsuarioResponsable()));
        queryHelper.agregarParametroValues("NOM_RESPEQUIVELACION", String.valueOf(valeSalida.getNombreResponsableEquipoVelacion()));
        queryHelper.agregarParametroValues("CVE_MATRICULARESPEQUIVELACION", String.valueOf(valeSalida.getMatriculaResponsableEquipoVelacion()));
        queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(valeSalida.getCantidadArticulos()));

        queryHelper.agregarParametroValues("ID_USUARIO_ALTA", String.valueOf(usuarioDto.getIdUsuario()));
        queryHelper.agregarParametroValues("FEC_ALTA", "CURRENT_TIMESTAMP");

        String queriesArticulos = crearDetalleVale(valeSalida.getArticulos());
        String query = queryHelper.obtenerQueryInsertar() + queriesArticulos;
        // todo - primero hay que insertar el registro del vale y recuperar el id
        // todo - ver como se genera el folio
        parametros.put(AppConstantes.QUERY, getBinary(query));
        parametros.put("separador", "$$");
        parametros.put("replace", "idTabla");
        datos.setDatos(parametros);
        return datos;
    }

    private String crearDetalleVale(List<DetalleValeSalidaRequest> articulos) {
        StringBuilder query = new StringBuilder();
        for (DetalleValeSalidaRequest detalleValeSalida : articulos) {
            QueryHelper queryHelper = new QueryHelper("INSERT INTO SVT_VALE_SALIDADETALLE");
            queryHelper.agregarParametroValues("ID_VALESALIDA", "idTabla");
            queryHelper.agregarParametroValues("ID_ARTICULO", String.valueOf(detalleValeSalida.getIdArticulo()));
            queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(detalleValeSalida.getCantidad()));
            queryHelper.agregarParametroValues("DES_OBSERVACION", String.valueOf(detalleValeSalida.getObservaciones()));
            query.append(" $$ ").append(queryHelper.obtenerQueryInsertar());
        }
        return query.toString();
    }

    // modificar vale de salida
    //   - hay que modificar tambien el registro de cada articulo que se modifique en el registro
    public String modificarVale(ValeSalidaDto valeSalida) {
        // todo - agregar solo los campos que se puedan modificar
        // que es lo que se va a modificar en esta parte?
        //
        QueryHelper queryHelper = new QueryHelper("update svt_vale_salida");
        queryHelper.agregarParametroValues("CAN_ARTICULOS", String.valueOf(valeSalida.getCantidadArticulos()));
        DatosRequest datos = new DatosRequest();

        return "";
    }

    // descontar el stock del inventario del velatorio
    //   - hay que crear el servicio en el catálogo de velatorios para que se consuma por el restTemplate
    //   - hay que actualizar el stock sumando o restando de dicho servicio

    /**
     * todo - quitar comentarios
     * todo - agregar documentacion
     *
     * @param articulo
     * @param restar
     * @return
     */
    public DatosRequest actualizarInventario(DetalleValeSalidaRequest articulo, boolean restar) {
//        QueryHelper queryHelper = new QueryHelper("update svt_inventario");
//        queryHelper.agregarParametroValues("", String.valueOf(articulo.getCantidad()));
//        queryHelper.addWhere("id_articulo = " + articulo.getIdArticulo());
        StringBuilder updateQuery = new StringBuilder("update svt_inventario ");
        if (restar) {
            updateQuery.append("CAN_STOCK = cantidad - ");
        } else {
            updateQuery.append("CAN_STOCK = cantidad + ");
        }

        updateQuery.append(articulo.getCantidad())
                .append(" ")
                .append("where id_articulo = ")
                .append(articulo.getIdArticulo());

        final String query = updateQuery.toString();
        DatosRequest datos = new DatosRequest();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        datos.setDatos(parametros);
        return datos;
    }

    private String restarInventario(Long idArticulo, int cantidad) {
        return "";
    }

    /**
     * @return
     */
    public DatosRequest consultarFoliosOds() {
        SelectQueryUtil queryUtil = new SelectQueryUtil();
        queryUtil.select("ID_ORDEN_SERVICIO as idOds",
                        "CVE_FOLIO as folioOds")
                .from("SVC_ORDEN_SERVICIO")
                .where("cve_estatus = 1");
        String query = getQuery(queryUtil);
        Map<String, Object> parametros = new HashMap<>();
        parametros.put(AppConstantes.QUERY, getBinary(query));
        DatosRequest datos = new DatosRequest();
        datos.setDatos(parametros);
        return datos;
    }

    public void consultarValeSalida() {
    }

    // registrar entrada de equipo
    //   - Se registra la fecha

    // generar el formato, ver si se usa la consulta del detalle del vale de salida

}
