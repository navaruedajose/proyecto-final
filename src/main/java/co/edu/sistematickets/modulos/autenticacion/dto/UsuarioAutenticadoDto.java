package co.edu.sistematickets.modulos.autenticacion.dto;

import co.edu.sistematickets.dominio.RolUsuario;

import java.time.Instant;
import java.util.UUID;

/**
 * Datos publicos del usuario autenticado.
 *
 * <p>Es la proyeccion que viaja en el inicio de sesion y en la consulta de
 * perfil. El campo {@code contrasenaHash} <strong>no existe</strong>, y esa
 * ausencia es la garantia: no se puede filtrar por descuido algo que no esta
 * en la clase. Es mas robusto que confiar en acordarse de excluirlo.</p>
 *
 * @param id             identificador del usuario
 * @param nombreCompleto nombre y apellidos
 * @param correo         correo electronico
 * @param rol            rol que determina sus permisos
 * @param departamento   area de la organizacion
 * @param telefono       telefono de contacto
 * @param activo         si la cuenta esta habilitada
 * @param ultimoAcceso   fecha del ultimo inicio de sesion correcto
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
