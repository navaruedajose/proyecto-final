package co.edu.sistematickets.modulos.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesión.
 *
 * <p>Solo dos campos: el correo y la contraseña. Cualquier otro dato que se
 * aceptara aquí (por ejemplo un rol propuesto por el cliente) sería una vía
 * para elevar privilegios, porque el rol lo decide el servidor leyendo la base
 * de datos, nunca la petición.</p>
 *
 * @param correo     correo electrónico registrado
 * @param contraseña contraseña en claro, solo en transito
 */
public record InicioSesionDto(

        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "Debe proporcionar un correo electrónico válido.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        String contrasena
) {
}
