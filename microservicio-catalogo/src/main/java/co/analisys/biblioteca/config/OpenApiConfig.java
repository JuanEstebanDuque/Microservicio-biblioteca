package co.analisys.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Catálogo")
                        .description("API para la gestión del catálogo de libros de la biblioteca universitaria. " +
                                "Permite consultar libros, gestionar su disponibilidad y realizar búsquedas.")
                        .version("1.0.0"));
    }
}
