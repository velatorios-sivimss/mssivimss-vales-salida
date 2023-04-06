package com.imss.sivimss.arquetipo.util;

import java.util.*;


/**
 * Utiler&iacute;a para crear consultas select.
 *
 * @author esa
 */
public class SelectQueryUtil {
    // constantes
    private static final String SELECT = "SELECT";
    private static final String SPACE = " ";
    private static final String FROM = "FROM";
    private static final String WHERE = "WHERE";
    private static final String LEFT_JOIN = "LEFT JOIN";
    private static final String JOIN = "JOIN";
    private static final String ON = "ON";
    private static final String OR = "OR";
    private static final String AND = "AND";
    private static final String ASTERISKS = "*";
    private static final String COLON = ":";
    private static final String ORDER_BY = "ORDER BY";
    private static final String GROUP_BY = "GROUP BY";
    // campos
    private final List<String> tablas = new ArrayList<>();
    private List<String> columnas = new ArrayList<>();
    private List<String> condiciones = new ArrayList<>();
    private Map<String, Object> parametros = new HashMap<>();
    private List<Join> joins = new ArrayList<>();
    private List<String> orderBy = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private String lastMethodCalled = "";
    private Join helperJoin;
    private boolean isFromCalled;
    private boolean isSelectCalled;
    private boolean isJoinCalled;

    /**
     * La funci&oacute;n <b>{@code select()}</b>, se tiene que invocar 2 veces, la primera es para crear una instancia de
     * <b>QueryUtil</b> y se llama sin pasar ning&uacute;n argumento, la segunda invocaci&oacute;n, en caso de ser
     * necesario, se pasan la lista de columnas que se van a usar para la consulta.
     * <p>
     * Los valores que recibe la funci&oacute;n puede ir de 1 a N, son valores separados por comas y en caso de
     * que se requieran todos los campos, la funci&oacute;n puede ir vac&iacte;a. Por ejemplo:
     * <p>
     * - <b>{@code select("columna_1 as id")}</b>
     * <p>
     * - <b>{@code select("columna_1 as id", "columna_2 as nombre", ...)}</b>
     * <p>
     * - <b>{@code select()}</b>
     *
     * @param columnas Lista de columnas, dichas columnas representan los valores que se van a recuperar
     *                 de la consulta.
     * @return Regresa la misma instancia para que se le puedan agregar m&aacute;s funciones.
     */
    public SelectQueryUtil select(String... columnas) {
        if (isFromCalled) {
            throw new IllegalStateException("No se puede llamar el from antes del select");
        }
        this.columnas = Arrays.asList(columnas);
        lastMethodCalled = SELECT;
        isSelectCalled = true;
        return this;
    }

    /**
     * Agrega la tabla o tablas necesarias para la consulta que se est&eacute; armando.
     * <p>
     * Se puede agregar, por ejemplo:
     * <p>
     * - Para solo una tabla: <b>{@code from("USUARIO as usuario")}</b>
     * <p>
     * - Para varias tablas: <b>{@code from("USUARIO as usuario", "ROL as rol")}</b>
     *
     * @param tabla Es una cadena que representa la o las tablas a las que va a realizar la consulta
     * @return Regresa la misma instancia para que se puedan anidar las otras funciones
     */
    public SelectQueryUtil from(String... tabla) {
        if (!isSelectCalled) {
            throw new IllegalStateException("No se puede llamar from sin haber agregado la sentencia select");
        }
        this.tablas.addAll(Arrays.asList(tabla));
        isFromCalled = true;
        lastMethodCalled = FROM;
        return this;
    }

    /**
     * La funci&oacute;n `where` se usa para agregar condiciones, estas pueden ser agregadas separadas por comas.
     * <p>
     * La sentencia where de sql se agrega hasta que se manda llamar la funci&oacute;n <b>{@code build()}</b>
     * <p>
     * Todas las condiciones que se agreguen mediante esta funci&oacute;n se anidar&aacute;n con un
     * operador <b>{@code AND}</b>
     *
     * @param condiciones Lista de condiciones que se van a evaluar en el query
     * @return
     */
    public SelectQueryUtil where(String... condiciones) {
        if (this.condiciones == null) {
            this.condiciones = new ArrayList<>();
        }

        this.condiciones.addAll(Arrays.asList(condiciones));
        lastMethodCalled = WHERE;
        return this;
    }

    /**
     * Agrega solo una condici&oacute;n a diferencia de <b>{@code where(String... condiciones}</b> esta
     * funci&oacute;n recibe solo una condici&oacute;n.
     *
     * @param condicion
     * @return
     */
    public SelectQueryUtil where(String condicion) {
        if (condiciones == null) {
            this.condiciones = new ArrayList<>();
        }
        this.condiciones.add(condicion);
        lastMethodCalled = WHERE;
        return this;
    }

    /**
     * Agrega el operador <b>{@code AND}</b> a la condici&oacute;n
     * <p>
     * Se debe colocar despu&eacute;s de la llamada a una funci&oacute;n <b>{@code where(...)}</b> o despu&eacute;s
     * de un <b>{@code join(...)}, {@code innerJoin(...)}, {@code leftJoin(...)}</b>
     *
     * @param condicion
     * @return
     */
    public SelectQueryUtil and(String condicion) {
        return validarCondicionesOrAnd(condicion, AND);
    }


    /**
     * Agrega una condicion SQL OR.
     * <p>
     * Se debe colocar despu&eacute;s de la llamada a una funci&oacute;n <b>{@code where(...)}</b> o despu&eacute;s
     * de un <b>{@code join(...)}, {@code innerJoin(...)}, {@code leftJoin(...)}</b>
     *
     * @param condicion
     * @return
     */
    public SelectQueryUtil or(String condicion) {
        return validarCondicionesOrAnd(condicion, OR);
    }

    /**
     * Valida si la la funci&oacute;n <b>{@code or(...)}</b> o la funci&oacute;n <b>{@code and(...)}</b>
     * @param condicion
     * @param or
     * @return
     */
    private SelectQueryUtil validarCondicionesOrAnd(String condicion, String or) {
        if (Objects.equals(lastMethodCalled, WHERE)) {
            if (condiciones.isEmpty()) {
                throw new IllegalStateException("Se tiene que agregar por lo menos una condicion en el where");
            }
            this.condiciones.add(crearCondicion(condicion, or));
        }
        if (lastMethodCalled.equals(JOIN)) {
            this.helperJoin.addOnCondition(crearCondicion(condicion, or));
        }
        return this;
    }

    /**
     * Agrega par&aacute;metros para que se pueda hacer la sustituci&oacute;n de dicho elemento para armar el query
     * con los valores que se agreguen en el mapa de par&aacute;metros.
     *
     * @param nombre
     * @param valor
     * @return
     */
    @SuppressWarnings("UnusedReturnValue")
    public SelectQueryUtil setParameter(String nombre, Object valor) {
        if (this.parametros == null) {
            this.parametros = new HashMap<>();
        }

        this.parametros.put(nombre, valor);
        return this;
    }

    /**
     * Agrega la sentencia SQL <b>{@code ORDER BY}</b> para ordenar la consulta.
     *
     * @param columna
     * @return
     */
    @SuppressWarnings("UnusedReturnValue")
    public SelectQueryUtil orderBy(String columna) {
        this.orderBy.add(columna);
        return this;
    }

    /**
     * Agrega la sentencia SQL <b>{@code GROUP BY}</b> para la consulta.
     *
     * @param columna
     * @return
     */
    public SelectQueryUtil groupBy(String columna) {
        this.groupBy.add(columna);
        return this;
    }

    /**
     * Agrega la sentencia de <b>{@code LEFT JOIN}</b> para hacer consultas con otras tablas.
     *
     * @param tabla
     * @param on
     * @return
     */
    public SelectQueryUtil leftJoin(String tabla, String... on) {
        helperJoin = new Join(LEFT_JOIN, tabla, on);
        joins.add(helperJoin);
        isJoinCalled = true;
        lastMethodCalled = JOIN;
        return this;
    }

    /**
     * Agrega la sentencia <b>{@code INNER JOIN}</b> para hacer consultas usando otras tablas.
     *
     * @param tabla
     * @param on
     * @return
     */
    public SelectQueryUtil innerJoin(String tabla, String on) {
        helperJoin = new Join(LEFT_JOIN, tabla, on);
        joins.add(helperJoin);
        isJoinCalled = true;
        lastMethodCalled = JOIN;
        return this;
    }

    /**
     * Agrega la sentencia <b>{@code JOIN}</b> para hacer consultas usando otras tablas.
     *
     * @param tabla
     * @param on
     * @return
     */
    public SelectQueryUtil join(String tabla, String on) {
        helperJoin = new Join(LEFT_JOIN, tabla, on);
        joins.add(helperJoin);
        isJoinCalled = true;
        lastMethodCalled = JOIN;
        return this;
    }

    /**
     * <b>{@code on(condiciones)}</b> es usado para agregar condiciones para una sentencia <b>{@code JOIN}</b>
     * se puede combinar para agregar varias condiciones al <b>{@code JOIN}</b> en cuesti&oacute;n.
     * <p>
     * Se debe llamar inmediatamente despu&eacute;s de la funci&oacute;n <b>{@code join(...)}</b> o <b>{@code leftJoin(...)}</b>
     *
     * @param condiciones
     * @return
     */
    public SelectQueryUtil on(String... condiciones) {
        if (!isJoinCalled && !lastMethodCalled.equals(JOIN)) {
            throw new IllegalStateException("on no puede se llamado sin antes invocar a join o joinLeft");
        }
        for (String condicion : condiciones) {
            helperJoin.addOnCondition(condicion);
        }
        return this;
    }

    /**
     * Regresa el query que se construy&oacute;.
     * <p>
     *
     * @return
     */
    public String build() {
        StringBuilder stringBuilder = new StringBuilder(SELECT);
        stringBuilder.append(SPACE);
        agregarColumnas(stringBuilder);
        agregarFrom(stringBuilder);
        agregarJoins(stringBuilder);
        agregarWhere(stringBuilder);
        addOrderBy(stringBuilder);
        addGroupBy(stringBuilder);

        return stringBuilder.toString();
    }

    /**
     * Agrega la sentencia <b>{@code FROM}</b> a la cadena para crear el <b>query sql</b>.
     *
     * @param stringBuilder
     */
    private void agregarFrom(StringBuilder stringBuilder) {
        stringBuilder.append(FROM).append(SPACE);
        stringBuilder.append(String.join(", ", tablas)).append(SPACE);
    }

    /**
     * Agrega la sentencia <b>{@code WHERE}</b> a la cadena para armar el <b>query sql</b>.
     *
     * @param stringBuilder
     */
    private void agregarWhere(StringBuilder stringBuilder) {
        if (validarCondiciones()) {
            stringBuilder.append(SPACE).append(WHERE).append(SPACE);
            agregarCondicionesWhere(stringBuilder);
        }
    }

    /**
     * Agrega los par&aacute;metros a los placeholders de forma din&aacute;mica.
     *
     * @param stringBuilder
     * @param index
     * @param condicion
     * @param helperCondiciones
     */
    private void agregarParametros(StringBuilder stringBuilder, int index, String condicion, List<String> helperCondiciones) {

        if (helperCondiciones.isEmpty()) {
            helperCondiciones = this.condiciones;
        }
        final boolean contieneOr = condicion.contains("#" + OR);
        final boolean contieneAndOr = contieneOr || condicion.contains("#" + AND);
        if (index != 0 &&
                !contieneAndOr) {
            stringBuilder.append(SPACE).append(AND).append(SPACE);
        }
        if (condicion.contains(COLON)) {
            String nombreParametro = condicion.substring(condicion.indexOf(COLON) + 1);
            Object value = parametros.get(nombreParametro);
            if (value instanceof String) {
                condicion = condicion.replace(COLON + nombreParametro, "'" + value.toString() + "'");
            } else {
                condicion = condicion.replace(COLON + nombreParametro, value.toString());
            }
        } else {
            condicion = condicion.trim();
        }
        if (contieneAndOr) {
            condicion = condicion.replace("#", "");
        }

        stringBuilder.append(condicion).append(SPACE);
    }


    /**
     * Agrega la senetencia order by en caso de que se exista.
     *
     * @param stringBuilder
     */
    private void addOrderBy(StringBuilder stringBuilder) {
        if (!orderBy.isEmpty()) {
            stringBuilder.append(SPACE)
                    .append(ORDER_BY)
                    .append(SPACE)
                    .append(String.join(", ", orderBy))
                    .append(SPACE);
        }
    }

    /**
     * Agrega la sentencia group by en caso de que exista
     *
     * @param stringBuilder
     */
    private void addGroupBy(StringBuilder stringBuilder) {
        if (!groupBy.isEmpty()) {
            stringBuilder.append(SPACE)
                    .append(GROUP_BY)
                    .append(SPACE)
                    .append(String.join(", ", groupBy))
                    .append(SPACE);
        }
    }

    /**
     * Agrega la lista de condiciones que se hayan agregado a la consulta.
     *
     * @param stringBuilder
     */
    private void agregarCondicionesWhere(StringBuilder stringBuilder) {
        if (validarCondiciones()) {
            for (int index = 0; index < condiciones.size(); index++) {
                String condicion = condiciones.get(index);
                agregarParametros(stringBuilder, index, condicion, new ArrayList<>());
            }
        }
    }

    /**
     * Valida que la lista de condiciones tenga alg&uacute;n valor.
     *
     * @return
     */
    private boolean validarCondiciones() {
        return !condiciones.isEmpty();
    }

    /**
     * Agrega los joins si es que se han agregado a la consulta.
     *
     * @param stringBuilder
     */
    private void agregarJoins(StringBuilder stringBuilder) {
        if (!joins.isEmpty()) {
            for (Join join : joins) {
                stringBuilder.append(SPACE);
                stringBuilder.append(join.getTipo());
                stringBuilder.append(SPACE);
                stringBuilder.append(join.getTabla());
                stringBuilder.append(SPACE).append(ON).append(SPACE);
                agregarCondicionesJoin(stringBuilder, join);
            }
        }
    }

    /**
     * Agrega la lista de condiciones para la sentencia <b>{@code JOIN}</b> que corresponda
     *
     * @param stringBuilder
     * @param join
     */
    private void agregarCondicionesJoin(StringBuilder stringBuilder, Join join) {
        final List<String> condicionesTemp = join.getOn();
        if (!condicionesTemp.isEmpty()) {
            for (int index = 0; index < condicionesTemp.size(); index++) {
                String condicion = condicionesTemp.get(index);
                agregarParametros(stringBuilder, index, condicion, condicionesTemp);
            }
        }
    }

    /**
     * Agrega la lista de columnas para construir la consulta.
     *
     * @param stringBuilder
     */
    private void agregarColumnas(StringBuilder stringBuilder) {
        if (columnas.isEmpty()) {
            stringBuilder.append(ASTERISKS).append(SPACE);
        } else {
            stringBuilder.append(String.join(", ", columnas)).append(SPACE);
        }
    }

    /**
     * Agrega una nueva condici&oacute;n dependiendo del tipo:
     * <p>
     * - AND
     * - OR
     *
     * @param condicion
     * @param tipo
     * @return
     */
    private static String crearCondicion(String condicion, String tipo) {
        return SPACE +
                "#" + tipo +
                SPACE +
                condicion;
    }

}
