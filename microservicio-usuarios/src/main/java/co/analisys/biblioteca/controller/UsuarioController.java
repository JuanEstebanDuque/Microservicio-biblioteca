package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.Email;
import co.analisys.biblioteca.model.Usuario;
import co.analisys.biblioteca.model.UsuarioId;
import co.analisys.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema de biblioteca universitaria")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Retorna los datos completos de un usuario dado su identificador único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public Usuario obtenerUsuario(
            @Parameter(description = "Identificador único del usuario") @PathVariable String id) {
        return usuarioService.obtenerUsuario(new UsuarioId(id));
    }

    @Operation(
            summary = "Obtener email de un usuario",
            description = "Retorna únicamente el email de un usuario. " +
                    "Este endpoint es consumido internamente por el microservicio de notificaciones."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email retornado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}/email")
    public String obtenerEmail(
            @Parameter(description = "Identificador único del usuario") @PathVariable String id) {
        return usuarioService.obtenerUsuario(new UsuarioId(id)).getEmail().getEmail_value();
    }

    @Operation(
            summary = "Cambiar email de un usuario",
            description = "Actualiza el email de un usuario existente en el sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/email")
    public void cambiarEmail(
            @Parameter(description = "Identificador único del usuario") @PathVariable String id,
            @RequestBody String nuevoEmail) {
        usuarioService.cambiarEmailUsuario(new UsuarioId(id), new Email(nuevoEmail));
    }
}
