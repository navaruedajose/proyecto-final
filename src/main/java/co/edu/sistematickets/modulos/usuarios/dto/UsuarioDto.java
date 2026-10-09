package co.edu.sistematickets.modulos.usuarios.dto;

import co.edu.sistematickets.dominio.RolUsuario;

import java.time.Instant;
import java.util.UUID;

/**
 * Usuario tal como se muestra en los listados y en el detalle.
 *
 * <p>Sin datos sensibles: no hay contrasena ni hash. Tambien se omite
 * {@code eliminado_en}: que un usuario este dado de baja se comunica con el
 * campo {@code activo}, que es lo que necesita saber el cliente.</p>
 *
 * @param id             identificador del usuario
 * @param nombreCompleto nombre y apellidos
 * @param correo         correo electronico
 * @param rol            rol y por tanto sus permisos
 * @param departamento   area de la organizacion
 * @param telefono       telefono de contacto
 * @param activo         si la cuenta esta habilitada
 * @param ultimoAcceso   ultimo inicio de sesion correcto
 * @param creadoEn       fecha de alta
 * @param ticketsAbiertos numero de tickets pendientes asignados, para repartir carga
 */
public record UsuarioDto(
        UUID id,
        String nombreCompleto,
        String correo,
        RolUsuario rol,
        String departamento,
        String telefono,
        boolean activo,
        Instant ultimoAcceso,
        Instant creadoEn,
        Long ticketsAbiertos
) {
}
