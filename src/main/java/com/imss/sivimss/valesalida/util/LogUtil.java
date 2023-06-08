package com.imss.sivimss.valesalida.util;

import com.google.gson.Gson;
import com.imss.sivimss.valesalida.model.request.UsuarioDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class LogUtil {
    @Value("${ruta-log}")
    private String rutaLog;
    @Value("${spring.application.name}")
    private String nombreAplicacion;

    private final String formatoFechaLog = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogUtil.class);


    public void crearArchivoLog(String tipoLog, String origen, String clasePath, String mensaje, String tiempoEjecucion, Authentication authentication) throws IOException {
        try {
            Gson json = new Gson();
            UsuarioDto usuarioDto = json.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
            File archivo = new File(rutaLog + nombreAplicacion + "_" + new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".log");
            FileWriter escribirArchivo = new FileWriter(archivo, true);
            if (archivo.exists()) {
                escribirArchivo.write("" + formatoFechaLog + " --- [" + tipoLog + "] " + origen + " " + clasePath + " : " + mensaje + " , Usuario: " + usuarioDto.getCveUsuario() + " - " + tiempoEjecucion);
                escribirArchivo.write("\r\n");
                escribirArchivo.close();
            } else {
                archivo.createNewFile();
                escribirArchivo.write("" + formatoFechaLog + " --- [" + tipoLog + "] " + origen + " " + clasePath + " : " + mensaje + " , Usuario: " + usuarioDto.getCveUsuario() + " - " + tiempoEjecucion);
                escribirArchivo.write("\r\n");
                escribirArchivo.close();
            }
        } catch (Exception e) {
            log.error("No se puede escribir el log.");
            log.error(e.getMessage());
        }

    }

    public void crearArchivoLogDTO(String tipoLog, String origen, String clasePath, String mensaje, String tiempoEjecucion, UsuarioDto usuarioDto) throws IOException {
        try {
            File archivo = new File(rutaLog + new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".log");
            FileWriter escribirArchivo = new FileWriter(archivo, true);
            if (archivo.exists()) {
                escribirArchivo.write("" + formatoFechaLog + " --- [" + tipoLog + "] " + origen + " " + clasePath + " : " + mensaje + " , Usuario: " + usuarioDto.getCveUsuario() + " - " + tiempoEjecucion);
                escribirArchivo.write("\r\n");
                escribirArchivo.close();
            } else {
                archivo.createNewFile();
                escribirArchivo.write("" + formatoFechaLog + " --- [" + tipoLog + "] " + origen + " " + clasePath + " : " + mensaje + " , Usuario: " + usuarioDto.getCveUsuario() + " - " + tiempoEjecucion);
                escribirArchivo.write("\r\n");
                escribirArchivo.close();
            }
        } catch (Exception e) {
            log.error("No se puede escribir el log.");
            log.error(e.getMessage());
        }

    }

}
