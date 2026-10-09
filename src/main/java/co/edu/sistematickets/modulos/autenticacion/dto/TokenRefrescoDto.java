package co.edu.sistematickets.modulos.autenticacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Peticion de renovacion o cierre de sesion.
 *
 * <p>El mismo DTO sirve para las dos operaciones porque ambas necesitan
 * exactamente el mismo dato: el token de refresco. Cerrar sesion consiste, a
 * efectos practicos, en revocar ese token en la base de datos.</p>
 *
 * @param tokenRefresco token de refresco emitido al iniciar sesion
 */
public record TokenRefrescoDto(

        @NotBlank(message = "El token de refresco es obligatorio.")
        @Size(min = 10, message = "El token de refresco no es valido.")
        String tokenRefresco
) {
}
