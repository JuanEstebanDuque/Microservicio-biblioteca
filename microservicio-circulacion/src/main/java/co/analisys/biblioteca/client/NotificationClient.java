package co.analisys.biblioteca.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import co.analisys.biblioteca.dto.NotificacionDTO;

@FeignClient(name = "notification-service", url = "http://localhost:8084")
public interface NotificationClient {

    @PostMapping("/notificar")
    void enviarNotificacion(@RequestBody NotificacionDTO notificacionDTO);

}