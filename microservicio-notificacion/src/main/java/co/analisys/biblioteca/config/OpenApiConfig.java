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
                        .title("Microservicio de Notificaciones")
                        .description("API para el envío de notificaciones a usuarios de la biblioteca. " +
                                "Consulta el servicio de usuarios para obtener el email del destinatario.")
                        .version("1.0.0"));
    }
}
