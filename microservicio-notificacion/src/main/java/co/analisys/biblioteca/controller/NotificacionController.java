package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.dto.NotificacionDTO;
import co.analisys.biblioteca.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificar")
@Tag(name = "Notificaciones", description = "Envío de notificaciones a usuarios de la biblioteca")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Operation(
            summary = "Enviar notificación a un usuario",
            description = "Recibe el ID del usuario y un mensaje, consulta el email real del usuario " +
                    "en el microservicio de usuarios y registra el envío de la notificación. " +
                    "Es invocado automáticamente por el microservicio de circulación en cada préstamo y devolución."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación enviada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado en el servicio de usuarios")
    })
    @PostMapping
    public void enviarNotificacion(@RequestBody NotificacionDTO notificacion) {
        notificacionService.enviarNotificacion(notificacion);
    }
}
