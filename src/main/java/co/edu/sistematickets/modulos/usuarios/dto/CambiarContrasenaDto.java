package co.edu.sistematickets.modulos.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contraseña por parte del propio usuario.
 *
 * <p>Exige la contraseña actual además de la nueva. No es una formalidad: sin
 * ese requisito, cualquiera que dejara la sesión abierta en un equipo público
 * podría cambiar la contraseña y quedarse con la cuenta. Pedirla obliga a
 * demostrar que quien cambia conoce la credencial vigente.</p>
 *
 * <p>Además del cambio, la operación <strong>revoca todas las sesiones</strong>
 * del usuario. Si no se hiciera, quien tuviera una sesión abierta seguiría
 * dentro con el token antiguo y el cambio no serviría de nada.</p>
 *
 * @param contrasenaActual contraseña vigente, para autorizar el cambio
 * @param contrasenaNueva  contraseña nueva, con las mismas reglas que el registro
 */
public record CambiarContrasenaDto(

        @NotBlank(message = "La contraseña actual es obligatoria.")
        String contrasenaActual,

        @NotBlank(message = "La contraseña nueva es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        @Pattern(regexp = ".*[A-Z].*", message = "La contraseña debe incluir al menos una letra mayúscula.")
        @Pattern(regexp = ".*[a-z].*", message = "La contraseña debe incluir al menos una letra minúscula.")
        @Pattern(regexp = ".*[0-9].*", message = "La contraseña debe incluir al menos un número.")
        String contrasenaNueva
) {
}
