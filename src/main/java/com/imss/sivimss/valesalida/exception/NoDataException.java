package com.imss.sivimss.valesalida.exception;

import com.imss.sivimss.valesalida.service.impl.ValeSalidaServiceImpl;

public class NoDataException extends Exception {
    private String codigo;
    public NoDataException(String message, String codigo) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }
}
