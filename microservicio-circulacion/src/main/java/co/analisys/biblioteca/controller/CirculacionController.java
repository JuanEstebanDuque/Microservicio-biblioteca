package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.model.Prestamo;
import co.analisys.biblioteca.model.PrestamoId;
import co.analisys.biblioteca.model.UsuarioId;
import co.analisys.biblioteca.service.CirculacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/circulacion")
@Tag(name = "Circulación", description = "Gestión de préstamos y devoluciones de libros")
public class CirculacionController {

    @Autowired
    private CirculacionService circulacionService;

    @Operation(
            summary = "Realizar préstamo de un libro",
            description = "Registra el préstamo de un libro a un usuario. " +
                    "Verifica la disponibilidad del libro en el microservicio de catálogo y " +
                    "envía una notificación al usuario a través del microservicio de notificaciones. " +
                    "El libro queda marcado como no disponible hasta su devolución."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El libro no está disponible"),
            @ApiResponse(responseCode = "404", description = "Usuario o libro no encontrado")
    })
    @PostMapping("/prestar")
    public void prestarLibro(
            @Parameter(description = "Identificador del usuario que realiza el préstamo") @RequestParam String usuarioId,
            @Parameter(description = "Identificador del libro a prestar") @RequestParam String libroId) {
        circulacionService.prestarLibro(new UsuarioId(usuarioId), new LibroId(libroId));
    }

    @Operation(
            summary = "Registrar devolución de un libro",
            description = "Marca un préstamo como devuelto, libera el libro en el catálogo y " +
                    "notifica al usuario que la devolución fue registrada exitosamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución registrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    @PostMapping("/devolver")
    public void devolverLibro(
            @Parameter(description = "Identificador del préstamo a devolver") @RequestParam String prestamoId) {
        circulacionService.devolverLibro(new PrestamoId(prestamoId));
    }

    @Operation(
            summary = "Consultar todos los préstamos",
            description = "Retorna una lista de todos los préstamos registrados en el sistema, " +
                    "tanto activos como devueltos. El usuario debe estar previamente registrado " +
                    "en la base de datos para poder operar con préstamos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de préstamos retornada correctamente")
    })
    @GetMapping("/prestamos")
    public List<Prestamo> obtenerTodosPrestamos() {
        return circulacionService.obtenerTodosPrestamos();
    }
}
