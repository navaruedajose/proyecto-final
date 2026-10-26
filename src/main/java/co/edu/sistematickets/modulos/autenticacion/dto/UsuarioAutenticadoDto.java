package co.edu.sistematickets.modulos.autenticacion.dto;

import co.edu.sistematickets.dominio.RolUsuario;

import java.time.Instant;
import java.util.UUID;

/**
 * Datos públicos del usuario autenticado.
 *
 * <p>Es la proyección que viaja en el inicio de sesión y en la consulta de
 * perfil. El campo {@code contrasenaHash} <strong>no existe</strong>, y esa
 * ausencia es la garantia: no se puede filtrar por descuido algo que no esta
 * en la clase. Es más robusto que confiar en acordarse de excluirlo.</p>
 *
 * @param id             identificador del usuario
 * @param nombreCompleto nombre y apellidos
 * @param correo         correo electrónico
 * @param rol            rol que determina sus permisos
 * @param departamento   área de la organización
 * @param teléfono       teléfono de contacto
 * @param activo         si la cuenta está habilitada
 * @param ultimoAcceso   fecha del último inicio de sesión correcto
 * @param creadoEn       fecha de alta de la cuenta
 */
public record UsuarioAutenticadoDto(
        UUID id,
        String nombreCompleto,
        String correo,
        RolUsuario rol,
        String departamento,
        String telefono,
        boolean activo,
        Instant ultimoAcceso,
        Instant creadoEn
) {
}
