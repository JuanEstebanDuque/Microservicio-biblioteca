package co.analisys.biblioteca.service;

import co.analisys.biblioteca.client.UsuarioClient;
import co.analisys.biblioteca.dto.NotificacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    @Autowired
    private UsuarioClient usuarioClient;

    public void enviarNotificacion(NotificacionDTO notificacion) {
        String email = usuarioClient.obtenerEmail(notificacion.getUsuarioId());
        System.out.println("Notificación enviada a " + email + ": " + notificacion.getMensaje());
    }
}