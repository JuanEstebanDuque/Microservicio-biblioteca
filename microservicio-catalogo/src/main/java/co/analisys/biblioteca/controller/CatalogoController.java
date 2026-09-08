package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.*;
import co.analisys.biblioteca.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
@Tag(name = "Catálogo", description = "Gestión del catálogo de libros de la biblioteca universitaria")
public class CatalogoController {

    private final CatalogoService catalogoService;

    @Autowired
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(
            summary = "Registrar un nuevo libro",
            description = "Crea un nuevo libro en el catálogo de la biblioteca."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro registrado correctamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    @PostMapping
    public void registrarLibro(@RequestBody RegistrarLibroRequest request) {
        Libro libro = new Libro();
        libro.setId(new LibroId(request.getId()));
        libro.setTitulo(request.getTitulo());
        libro.setIsbn(new ISBN(request.getIsbn()));
        libro.setCategoria(new Categoria(request.getCategoria()));
        libro.setDisponible(request.isDisponible());
        catalogoService.registrarLibro(libro);
    }

    @Operation(
            summary = "Listar todos los libros",
            description = "Retorna todos los libros registrados en el catálogo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de libros retornada correctamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    @GetMapping
    public List<Libro> obtenerTodosLosLibros() {
        return catalogoService.obtenerTodosLosLibros();
    }

    @Operation(
            summary = "Obtener libro por ID",
            description = "Retorna los datos completos de un libro dado su identificador único, incluyendo autores y disponibilidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    @GetMapping("/{id}")
    public Libro obtenerLibro(
            @Parameter(description = "Identificador único del libro") @PathVariable String id) {
        return catalogoService.obtenerLibro(new LibroId(id));
    }

    @Operation(
            summary = "Consultar disponibilidad de un libro",
            description = "Retorna true si el libro está disponible para préstamo, false si ya está prestado. " +
                    "Este endpoint es consumido internamente por el microservicio de circulación."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad consultada correctamente"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    @GetMapping("/{id}/disponible")
    public boolean isLibroDisponible(
            @Parameter(description = "Identificador único del libro") @PathVariable String id) {
        Libro libro = catalogoService.obtenerLibro(new LibroId(id));
        return libro != null && libro.isDisponible();
    }

    @Operation(
            summary = "Actualizar disponibilidad de un libro",
            description = "Marca un libro como disponible (true) o no disponible (false). " +
                    "Es llamado por el microservicio de circulación al realizar un préstamo o devolución."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada correctamente"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    @PutMapping("/{id}/disponibilidad")
    public void actualizarDisponibilidad(
            @Parameter(description = "Identificador único del libro") @PathVariable String id,
            @RequestBody boolean disponible) {
        catalogoService.actualizarDisponibilidad(new LibroId(id), disponible);
    }

    @Operation(
            summary = "Buscar libros por criterio",
            description = "Retorna una lista de libros cuyo título coincide con el criterio de búsqueda proporcionado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente")
    })
    @GetMapping("/buscar")
    public List<Libro> buscarLibros(
            @Parameter(description = "Texto a buscar en el título del libro") @RequestParam String criterio) {
        return catalogoService.buscarLibros(criterio);
    }
}
