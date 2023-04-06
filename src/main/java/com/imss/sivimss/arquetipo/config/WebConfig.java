package com.imss.sivimss.arquetipo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase para habilitar los cors (las peticiones desde un cliente como react, vue, angular)
 * @author pnolasco
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedMethods("*")
				.allowedOrigins("*")
				.allowedHeaders("*");
	}
}
