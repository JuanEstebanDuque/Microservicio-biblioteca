package co.analisys.biblioteca.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrarLibroRequest {
    private String id;
    private String titulo;
    private String isbn;
    private String categoria;
    private boolean disponible;
}
