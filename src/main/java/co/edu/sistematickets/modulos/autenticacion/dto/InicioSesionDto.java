package co.edu.sistematickets.modulos.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesion.
 *
 * <p>Solo dos campos: el correo y la contrasena. Cualquier otro dato que se
 * aceptara aqui (por ejemplo un rol propuesto por el cliente) seria una via
 * para elevar privilegios, porque el rol lo decide el servidor leyendo la base
 * de datos, nunca la peticion.</p>
 *
 * @param correo     correo electronico registrado
 * @param contrasena contrasena en claro, solo en transito
 */
public record InicioSesionDto(

        @NotBlank(message = "El correo electronico es obligatorio.")
        @Email(message = "Debe proporcionar un correo electronico valido.")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria.")
        String contrasena
) {
}
