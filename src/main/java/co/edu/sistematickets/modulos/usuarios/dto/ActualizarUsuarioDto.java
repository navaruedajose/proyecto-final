package co.edu.sistematickets.modulos.usuarios.dto;

import co.edu.sistematickets.dominio.RolUsuario;
import jakarta.validation.constraints.Size;

/**
 * Datos editables de un usuario.
 *
 * <p>Lo que no se puede cambiar por aqui, y por que:</p>
 * <ul>
 *   <li>{@code correo} — es el identificador de acceso. Cambiarlo es una
 *       operacion delicada (requiere verificar el nuevo buzon), asi que tiene
 *       su propio flujo y no se mezcla con la edicion de perfil.</li>
 *   <li>{@code contrasena} — tiene su propio endpoint, donde se exige la
 *       contrasena actual. Permitir cambiarla desde aqui abriria la puerta a
 *       que un administrador tomara cuentas ajenas cambiandoles la clave.</li>
 * </ul>
 *
 * <p>{@code rol} y {@code activo} solo los puede tocar un administrador; que
 * esten en el mismo DTO es comodidad de transporte. La restriccion real vive
 * en el servicio.</p>
 *
 * @param nombreCompleto nuevo nombre, o {@code null}
 * @param telefono       nuevo telefono, o {@code null}
 * @param departamento   nuevo departamento, o {@code null}
 * @param rol            nuevo rol; solo administradores
 * @param activo         habilitar o deshabilitar la cuenta; solo administradores
 */
public record ActualizarUsuarioDto(

        @Size(min = 3, max = 150, message = "El nombre completo debe tener entre 3 y 150 caracteres.")
        String nombreCompleto,

        @Size(max = 25, message = "El telefono no puede exceder 25 caracteres.")
        String telefono,

        @Size(max = 100, message = "El departamento no puede exceder 100 caracteres.")
        String departamento,

        RolUsuario rol,

        Boolean activo
) {

    /**
     * Indica si la peticion no trae ningun campo que actualizar.
     *
     * @return {@code true} si todos los campos son {@code null}
     */
    public boolean estaVacio() {
        return nombreCompleto == null && telefono == null && departamento == null
                && rol == null && activo == null;
    }
}
