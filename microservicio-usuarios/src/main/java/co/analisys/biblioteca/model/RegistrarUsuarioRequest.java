package co.analisys.biblioteca.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrarUsuarioRequest {
    private String id;
    private String nombre;
    private String email;
    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String username;
    private String passwordHash;
}
