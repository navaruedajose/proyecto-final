package co.edu.sistematickets.modulos.autenticacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición de renovación o cierre de sesión.
 *
 * <p>El mismo DTO sirve para las dos operaciones porque ambas necesitan
 * exactamente el mismo dato: el token de refresco. Cerrar sesión consiste, a
 * efectos practicos, en revocar ese token en la base de datos.</p>
 *
 * @param tokenRefresco token de refresco emitido al iniciar sesión
 */
public record TokenRefrescoDto(

        @NotBlank(message = "El token de refresco es obligatorio.")
        @Size(min = 10, message = "El token de refresco no es válido.")
        String tokenRefresco
) {
}
