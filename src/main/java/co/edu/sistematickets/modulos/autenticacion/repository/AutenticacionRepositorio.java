package co.edu.sistematickets.modulos.autenticacion.repository;

import co.edu.sistematickets.modulos.autenticacion.dto.UsuarioAutenticadoDto;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de autenticacion.
 *
 * <p>Separado del repositorio de usuarios a proposito, aunque ambos toquen la
 * misma tabla. Las operaciones de autenticacion tienen requisitos distintos:
 * necesitan leer el hash de la contrasena (algo que la gestion de usuarios no
 * deberia poder hacer nunca) y trabajan con los tokens de refresco. Mezclar
 * ambos repositorios haria que el modulo de usuarios tuviera acceso a
 * credenciales sin necesitarlo.</p>
 *
 * <p><strong>Estado actual: solo la interfaz.</strong></p>
 */
public interface AutenticacionRepositorio {

    /**
     * Busca un usuario por su correo, incluyendo el hash de la contrasena.
     *
     * <p>Es el unico punto del sistema que devuelve el hash. Se aísla aqui
     * para que sea facil de auditar y para que la regla «el hash no sale de
     * este repositorio» sea verificable de un vistazo.</p>
     *
     * @param correo correo electronico, ya normalizado a minusculas
     * @return el usuario con su hash de contrasena, o vacio si no existe
     */
    Optional<Object> buscarPorCorreoConCredenciales(String correo);

    /**
     * Comprueba si un correo ya esta registrado.
     *
     * <p>Se consulta antes de insertar para devolver un error claro, en lugar
     * de dejar que la restriccion de unicidad de la base produzca un error
     * tecnico. La restriccion sigue estando: esta comprobacion mejora el
     * mensaje, no sustituye a la integridad de la base.</p>
     *
     * @param correo correo a comprobar
     * @return {@code true} si ya existe una cuenta con ese correo
     */
    boolean existePorCorreo(String correo);

    /**
     * Registra un intento de acceso correcto.
     *
     * <p>Actualiza {@code ultimo_acceso}, dato que se muestra en el perfil y
     * que ayuda a detectar accesos no reconocidos.</p>
     *
     * @param usuarioId identificador del usuario
     * @param momento   instante del acceso
     */
    void registrarAcceso(UUID usuarioId, Instant momento);

    /**
     * Guarda el hash de un token de refresco.
     *
     * @param usuarioId  dueño del token
     * @param tokenHash  hash del token; nunca el token en claro
     * @param expiraEn   momento de caducidad
     */
    void guardarTokenRefresco(UUID usuarioId, String tokenHash, Instant expiraEn);

    /**
     * Busca la sesion asociada a un token de refresco.
     *
     * @param tokenHash hash del token recibido
     * @return la sesion, o vacio si el token no existe o ya fue revocado
     */
    Optional<Object> buscarTokenRefresco(String tokenHash);

    /**
     * Revoca un token de refresco concreto.
     *
     * @param tokenHash hash del token a revocar
     * @return {@code true} si se revoco algo
     */
    boolean revocarTokenRefresco(String tokenHash);

    /**
     * Revoca todos los tokens de refresco de un usuario.
     *
     * <p>Se usa tras un cambio de contrasena y en la operacion de cerrar todas
     * las sesiones. Si no se hiciera, cambiar la contrasena no expulsaria a
     * quien ya tuviera una sesion abierta, que es justo lo contrario de lo que
     * se espera al cambiarla.</p>
     *
     * @param usuarioId identificador del usuario
     * @return numero de sesiones revocadas
     */
    int revocarTodosLosTokens(UUID usuarioId);

    /**
     * Devuelve el perfil publico de un usuario.
     *
     * @param usuarioId identificador del usuario
     * @return perfil sin datos sensibles
     */
    Optional<UsuarioAutenticadoDto> buscarPerfil(UUID usuarioId);
}
