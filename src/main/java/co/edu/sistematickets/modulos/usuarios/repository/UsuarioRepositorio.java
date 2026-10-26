package co.edu.sistematickets.modulos.usuarios.repository;

import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.usuarios.dto.UsuarioDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de usuarios.
 *
 * <p><strong>Estado actual: solo la interfaz.</strong> Las implementaciones
 * reales llegaran con la capa de persistencia.</p>
 *
 * <p>Ningún método devuelve el hash de la contraseña: esa lectura vive
 * unicamente en {@code AutenticacionRepositorio}. Es una separación
 * deliberada, para que el módulo de gestion de usuarios no pueda leer
 * credenciales ni por descuido.</p>
 */
public interface UsuarioRepositorio {

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return el usuario, o vacío si no existe
     */
    Optional<UsuarioDto> buscarPorId(UUID id);

    /**
     * Busca un usuario por su correo.
     *
     * @param correo correo electrónico
     * @return el usuario, o vacío si no existe
     */
    Optional<UsuarioDto> buscarPorCorreo(String correo);

    /**
     * Lista usuarios con filtros de rol, estado y búsqueda de texto.
     *
     * @param rol      filtra por rol; {@code null} para todos
     * @param activo   filtra por cuentas habilitadas; {@code null} para todas
     * @param búsqueda texto libre sobre nombre y correo
     * @param página   número de página, empezando en 1
     * @param límite   tamaño de página
     * @return lista de usuarios
     */
    List<UsuarioDto> listar(RolUsuario rol, Boolean activo, String busqueda, int pagina, int limite);

    /**
     * Cuenta los usuarios que cumplen los filtros.
     *
     * @param rol      filtro por rol; puede ser {@code null}
     * @param activo   filtro por estado; puede ser {@code null}
     * @param búsqueda texto libre; puede ser {@code null}
     * @return total de coincidencias
     */
    long contar(RolUsuario rol, Boolean activo, String busqueda);

    /**
     * Devuelve los agentes ordenados por carga de trabajo.
     *
     * <p>El orden importa: alimenta la sugerencia de asignación, y repartir
     * entre quien menos tiene es más justo y equilibra los tiempos de
     * respuesta. Se apoya en la vista {@code vista_desempeno_agentes}.</p>
     *
     * @return agentes de menor a mayor número de tickets abiertos
     */
    List<UsuarioDto> listarAgentesPorCarga();

    /**
     * Persiste los cambios de un usuario.
     *
     * @param id      identificador del usuario
     * @param usuario datos a persistir
     * @return el usuario actualizado
     */
    UsuarioDto actualizar(UUID id, UsuarioDto usuario);

    /**
     * Desactiva una cuenta (borrado lógico).
     *
     * <p>No se borra la fila: el usuario aparece como autor en tickets,
     * comentarios e historial, y borrarlo dejaría el rastro de la auditoría
     * apuntando a un identificador inexistente.</p>
     *
     * @param id identificador del usuario
     */
    void desactivar(UUID id);

    /**
     * Actualiza el hash de la contraseña.
     *
     * @param id           identificador del usuario
     * @param nuevoHash    nuevo hash bcrypt
     */
    void actualizarContrasena(UUID id, String nuevoHash);

    /**
     * Devuelve el hash de la contraseña actual de un usuario.
     *
     * <p>Es la única lectura de credenciales de este repositorio y existe por
     * un solo motivo: verificar la contraseña actual antes de cambiarla. Se
     * documenta aquí para que quede claro que es una excepción deliberada y no
     * un descuido.</p>
     *
     * @param id identificador del usuario
     * @return hash bcrypt de la contraseña vigente
     */
    Optional<String> obtenerHashContrasena(UUID id);
}
