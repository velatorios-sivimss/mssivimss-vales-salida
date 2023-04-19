package com.imss.sivimss.valesalida.controller;

import com.imss.sivimss.valesalida.service.ValeSalidaService;
import com.imss.sivimss.valesalida.util.DatosRequest;
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
import java.util.concurrent.CompletableFuture;

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

    public ValeSalidaController(ValeSalidaService valeSalidaService, ProviderServiceRestTemplate restTemplate) {
        this.valeSalidaService = valeSalidaService;
        this.restTemplate = restTemplate;
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
    @PostMapping("/consulta-filtros")
    public CompletableFuture<?> consultarValesPorFiltros(@RequestBody DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = valeSalidaService.consultarVales(request, authentication);
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
