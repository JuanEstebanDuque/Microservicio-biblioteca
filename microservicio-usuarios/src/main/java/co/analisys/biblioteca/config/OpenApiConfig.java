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
                        .title("Microservicio de Usuarios")
                        .description("API para la gestión de usuarios del sistema de biblioteca universitaria. " +
                                "Permite consultar, crear y modificar usuarios (estudiantes, profesores y personal).")
                        .version("1.0.0"));
    }
}
