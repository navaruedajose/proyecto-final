package co.edu.sistematickets.modulos.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de registro de una cuenta nueva.
 *
 * <p>Las reglas de la contraseña se copian del esquema del proyecto original
 * para que las dos implementaciones acepten exactamente lo mismo: mínimo 8
 * caracteres, una mayúscula, una minúscula y un número. El máximo de 72 no es
 * caprichoso: es el límite de entrada de bcrypt, y aceptar más caracteres
 * daria una falsa sensación de seguridad, porque a partir de ahi el hash
 * ignora el resto.</p>
 *
 * <p>El campo {@code rol} no existe a propósito. Un registro público
 * <strong>siempre</strong> crea un {@code SOLICITANTE}; si se aceptara el rol
 * del cliente, cualquiera podría darse de alta como administrador. Las cuentas
 * internas se crean por otro endpoint, y solo un administrador puede usarlo.</p>
 *
 * @param nombreCompleto nombre y apellidos del usuario
 * @param correo         correo electrónico, que será su nombre de usuario
 * @param contraseña     contraseña en claro, solo en transito
 * @param teléfono       teléfono de contacto, opcional (para avisos por SMS)
 * @param departamento   área de la organización, opcional
 */
public record RegistroDto(

        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(min = 3, max = 150, message = "El nombre completo debe tener entre 3 y 150 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "Debe proporcionar un correo electrónico válido.")
        @Size(max = 180, message = "El correo no puede exceder 180 caracteres.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        @Pattern(regexp = ".*[A-Z].*", message = "La contraseña debe incluir al menos una letra mayúscula.")
        @Pattern(regexp = ".*[a-z].*", message = "La contraseña debe incluir al menos una letra minúscula.")
        @Pattern(regexp = ".*[0-9].*", message = "La contraseña debe incluir al menos un número.")
        String contrasena,

        @Pattern(regexp = "^$|^[+]?[0-9\\s()-]{7,25}$", message = "El teléfono no tiene un formato válido.")
        String telefono,

        @Size(max = 100, message = "El departamento no puede exceder 100 caracteres.")
        String departamento
) {
}
