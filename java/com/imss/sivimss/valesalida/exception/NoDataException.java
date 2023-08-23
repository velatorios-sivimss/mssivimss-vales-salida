package com.imss.sivimss.valesalida.exception;

/**
 * @author esa
 */
public class NoDataException extends Exception {

    private static final String MSG85_NO_SE_ENCONTRARON_RESULTADOS = "85";
    private final String codigo;
    public NoDataException(String message, String codigo) {
        super(message);
        this.codigo = codigo;
    }
    public NoDataException(String message) {
        super(message);
        this.codigo = MSG85_NO_SE_ENCONTRARON_RESULTADOS;
    }

    public String getCodigo() {
        return this.codigo;
    }
}
