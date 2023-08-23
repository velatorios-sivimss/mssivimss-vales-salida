package com.imss.sivimss.valesalida.model.request;

import com.imss.sivimss.valesalida.exception.ValidacionFechasException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class FiltrosRequest {
    private Long idNivel;
    private Long idDelegacion;
    private Long idVelatorio;
    private Long idValeSalida;
    private String folioOds;
    private String fechaInicio;
    private String fechaFinal;

    /**
     * Verifica si todos los elementos en el objeto son nulos.
     *
     * @return <b>{@code true}</b> o <b>{@code false}</b> dependiendo si todas las propiedades del objeto son
     * nulas o no.
     */
    public boolean validarNulos() {
        return idDelegacion == null &&
                idVelatorio == null &&
                folioOds == null &&
                fechaInicio == null && fechaFinal == null;
    }

    /**
     * Compara fechas para que la <b>{@code fechaInicio}</b> no sea mayor a la <b>{@code fechaFinal}</b>
     *
     * @return <b>{@code true}</b> o <b>{@code false}</b> dependiendo si la fecha inicial es mayor a la final.
     */
    public boolean validarFechas() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date fecha1 = sdf.parse(fechaInicio);
            Date fecha2 = sdf.parse(fechaFinal);

            final int i = fecha1.compareTo(fecha2);
            return i <= 0;
        } catch (ParseException e) {
            throw new ValidacionFechasException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
