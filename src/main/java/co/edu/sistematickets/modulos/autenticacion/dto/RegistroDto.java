package co.edu.sistematickets.modulos.autenticacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de registro de una cuenta nueva.
 *
 * <p>Las reglas de la contrasena se copian del esquema del proyecto original
 * para que las dos implementaciones acepten exactamente lo mismo: minimo 8
 * caracteres, una mayuscula, una minuscula y un numero. El maximo de 72 no es
 * caprichoso: es el limite de entrada de bcrypt, y aceptar mas caracteres
 * daria una falsa sensacion de seguridad, porque a partir de ahi el hash
 * ignora el resto.</p>
 *
 * <p>El campo {@code rol} no existe a proposito. Un registro publico
 * <strong>siempre</strong> crea un {@code SOLICITANTE}; si se aceptara el rol
 * del cliente, cualquiera podria darse de alta como administrador. Las cuentas
 * internas se crean por otro endpoint, y solo un administrador puede usarlo.</p>
 *
 * @param nombreCompleto nombre y apellidos del usuario
 * @param correo         correo electronico, que sera su nombre de usuario
 * @param contrasena     contrasena en claro, solo en transito
 * @param telefono       telefono de contacto, opcional (para avisos por SMS)
 * @param departamento   area de la organizacion, opcional
 */
public record RegistroDto(

        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(min = 3, max = 150, message = "El nombre completo debe tener entre 3 y 150 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo electronico es obligatorio.")
        @Email(message = "Debe proporcionar un correo electronico valido.")
        @Size(max = 180, message = "El correo no puede exceder 180 caracteres.")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria.")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres.")
        @Pattern(regexp = ".*[A-Z].*", message = "La contrasena debe incluir al menos una letra mayuscula.")
        @Pattern(regexp = ".*[a-z].*", message = "La contrasena debe incluir al menos una letra minuscula.")
        @Pattern(regexp = ".*[0-9].*", message = "La contrasena debe incluir al menos un numero.")
        String contrasena,

        @Pattern(regexp = "^$|^[+]?[0-9\\s()-]{7,25}$", message = "El telefono no tiene un formato valido.")
        String telefono,

        @Size(max = 100, message = "El departamento no puede exceder 100 caracteres.")
        String departamento
) {
}
