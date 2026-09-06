package co.analisys.biblioteca.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuario-service", url = "http://localhost:8081")
public interface UsuarioClient {

    @GetMapping("/usuarios/{usuarioId}/email")
    String obtenerEmail(@PathVariable("usuarioId") String usuarioId);
}
