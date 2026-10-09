package co.edu.sistematickets.modulos.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contrasena por parte del propio usuario.
 *
 * <p>Exige la contrasena actual ademas de la nueva. No es una formalidad: sin
 * ese requisito, cualquiera que dejara la sesion abierta en un equipo publico
 * podria cambiar la contrasena y quedarse con la cuenta. Pedirla obliga a
 * demostrar que quien cambia conoce la credencial vigente.</p>
 *
 * <p>Ademas del cambio, la operacion <strong>revoca todas las sesiones</strong>
 * del usuario. Si no se hiciera, quien tuviera una sesion abierta seguiria
 * dentro con el token antiguo y el cambio no serviria de nada.</p>
 *
 * @param contrasenaActual contrasena vigente, para autorizar el cambio
 * @param contrasenaNueva  contrasena nueva, con las mismas reglas que el registro
 */
public record CambiarContrasenaDto(

        @NotBlank(message = "La contrasena actual es obligatoria.")
        String contrasenaActual,

        @NotBlank(message = "La contrasena nueva es obligatoria.")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres.")
        @Pattern(regexp = ".*[A-Z].*", message = "La contrasena debe incluir al menos una letra mayuscula.")
        @Pattern(regexp = ".*[a-z].*", message = "La contrasena debe incluir al menos una letra minuscula.")
        @Pattern(regexp = ".*[0-9].*", message = "La contrasena debe incluir al menos un numero.")
        String contrasenaNueva
) {
}
