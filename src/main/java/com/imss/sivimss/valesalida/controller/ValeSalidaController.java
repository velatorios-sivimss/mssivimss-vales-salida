package com.imss.sivimss.valesalida.controller;

import com.imss.sivimss.valesalida.service.ValeSalidaService;
import com.imss.sivimss.valesalida.util.DatosRequest;
import com.imss.sivimss.valesalida.util.LogUtil;
import com.imss.sivimss.valesalida.util.ProviderServiceRestTemplate;
import com.imss.sivimss.valesalida.util.Response;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Controller con los endpoints para administrar los vales de salida
 *
 * @author esa
 */
@RestController
@RequestMapping("/vales-salida")
public class ValeSalidaController {

    private final ValeSalidaService valeSalidaService;
    private final ProviderServiceRestTemplate restTemplate;
    private final LogUtil logUtil;
    private static final String ALTA = "alta";
    private static final String BAJA = "baja";
    private static final String MODIFICACION = "modificacion";
    private static final String CONSULTA = "consulta";


    public ValeSalidaController(ValeSalidaService valeSalidaService, ProviderServiceRestTemplate restTemplate, LogUtil logUtil) {
        this.valeSalidaService = valeSalidaService;
        this.restTemplate = restTemplate;
        this.logUtil = logUtil;
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/consultar-detalle")
    public CompletableFuture<?> consultarDetalle(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.consultarDetalle(request, authentication);
        return CompletableFuture.supplyAsync(
                () -> getResponseEntity(response)
        );
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping
    public CompletableFuture<?> consultarVales(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.consultarVales(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/consultar-folios-ods")
    public CompletableFuture<?> consultarFoliosOds(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.consultarCatalogoOds(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/consultar-datos-ods")
    public CompletableFuture<?> consultarDatosRegistro(@RequestBody DatosRequest request, Authentication authentication) throws IOException, ParseException {
        Response<?> response = valeSalidaService.consultarDatosPantallaRegistro(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/consultar-con-filtros")
    public CompletableFuture<?> consultarValesPorFiltros(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.consultarValesFiltros(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/crear")
    public CompletableFuture<?> crearVale(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.crearVale(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/actualizar")
    public CompletableFuture<?> actualizarVale(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.modificarVale(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/registrar-entrada")
    public CompletableFuture<?> registrarEntradaVale(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.registrarEntrada(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/cambiar-estatus")
    public CompletableFuture<?> cambiarEstatus(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.cambiarEstatus(request, authentication);
        return CompletableFuture.supplyAsync(() -> getResponseEntity(response));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/descargar-reporte")
    public CompletableFuture<?> descargarReporte(@RequestBody DatosRequest request, Authentication authentication)
            throws IOException, ParseException {

        Response<?> response = valeSalidaService.generarReportePdf(request, authentication);
        return CompletableFuture
                .supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
    }

    @CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
    @TimeLimiter(name = "msflujo")
    @PostMapping("/descargar-reporte-tabla")
    public CompletableFuture<?> descargarReporteTabla(@RequestBody DatosRequest request, Authentication authentication) throws IOException, ParseException {
        Response<?> response = valeSalidaService.generarReporteTabla(request, authentication);
        return CompletableFuture
                .supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
    }

    /**
     * Crea el responseEntity para contestar la petici&oacute;n.
     *
     * @param response
     * @return
     */
    private static ResponseEntity<? extends Response<?>> getResponseEntity(Response<?> response) {
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo()));
    }

    /**
     * fallbacks generico
     *
     * @return respuestas
     */
    private CompletableFuture<?> fallbackGenerico(@RequestBody DatosRequest request, Authentication authentication,
                                                  CallNotPermittedException e) {
        Response<?> response = restTemplate.respuestaProvider(e.getMessage());
        return CompletableFuture
                .supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
    }

    private CompletableFuture<?> fallbackGenerico(@RequestBody DatosRequest request, Authentication authentication,
                                                  RuntimeException e) {
        Response<?> response = restTemplate.respuestaProvider(e.getMessage());
        return CompletableFuture
                .supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
    }

    private CompletableFuture<?> fallbackGenerico(@RequestBody DatosRequest request, Authentication authentication,
                                                  NumberFormatException e) {
        Response<?> response = restTemplate.respuestaProvider(e.getMessage());
        return CompletableFuture
                .supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
    }
}
