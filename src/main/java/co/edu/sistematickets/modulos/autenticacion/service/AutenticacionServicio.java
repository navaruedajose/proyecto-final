package co.edu.sistematickets.modulos.autenticacion.service;

import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.autenticacion.dto.InicioSesionDto;
import co.edu.sistematickets.modulos.autenticacion.dto.RegistroDto;
import co.edu.sistematickets.modulos.autenticacion.dto.SesionDto;
import co.edu.sistematickets.modulos.autenticacion.dto.UsuarioAutenticadoDto;

import java.util.UUID;

/**
 * Reglas de negocio de autenticación y sesión.
 *
 * <p>Es el módulo con más implicaciones de seguridad, así que conviene tener
 * claras las decisiones que toma:</p>
 *
 * <ol>
 *   <li><strong>Un registro público siempre crea un {@code SOLICITANTE}.</strong>
 *       El rol no se acepta del cliente. Si se aceptara, registrarse como
 *       administrador sería cuestión de editar el cuerpo de la petición.</li>
 *   <li><strong>Un correo desconocido y una contraseña incorrecta dan el
 *       mismo error.</strong> Distinguirlos permitiría averiguar que correos
 *       están registrados probando uno por uno. El mensaje debe ser genérico y
 *       el mismo en los dos casos.</li>
 *   <li><strong>La comparación de la contraseña es la de bcrypt</strong>, que
 *       tarda lo mismo acierte o falle. Un {@code equals} de cadenas normal
 *       filtraria información por el tiempo que tarda en fallar.</li>
 *   <li><strong>Cambiar la contraseña revoca todas las sesiones.</strong> Si no
 *       se hiciera, quien tuviera una sesión abierta seguiría dentro después
 *       del cambio.</li>
 * </ol>
 */
public class AutenticacionServicio {

    /**
     * @param datos datos de registro
     * @return perfil del usuario creado
     */
    public UsuarioAutenticadoDto registrar(RegistroDto datos) {
        throw new UnsupportedOperationException(
                "El registro se implementara al conectar la persistencia. "
                        + "Recordar: el rol será siempre SOLICITANTE.");
    }

    /**
     * Crea una cuenta interna (agente o administrador).
     *
     * <p>Requiere ser administrador. Es el único camino por el que un usuario
     * puede nacer con rol distinto de {@code SOLICITANTE}.</p>
     *
     * @param datos          datos de la cuenta
     * @param rolSolicitado  rol a asignar
     * @param adminId        administrador que realiza el alta
     * @return perfil del usuario creado
     */
    public UsuarioAutenticadoDto registrarUsuarioInterno(RegistroDto datos, RolUsuario rolSolicitado, UUID adminId) {
        throw new UnsupportedOperationException(
                "El alta de cuentas internas se implementara al conectar la persistencia.");
    }

    /**
     * Inicia sesión y emite los dos tokens.
     *
     * <p>El mensaje de error será el mismo tanto si el correo no existe como
     * si la contraseña es incorrecta. Es una decisión de seguridad, no un
     * descuido: cualquier mensaje más específico convierte el inicio de sesión
     * en una herramienta para enumerar cuentas.</p>
     *
     * @param credenciales correo y contraseña
     * @return sesión con los dos tokens y el perfil
     */
    public SesionDto iniciarSesion(InicioSesionDto credenciales) {
        throw new UnsupportedOperationException(
                "El inicio de sesión se implementara al conectar la persistencia. "
                        + "Recordar: mismo mensaje para correo inexistente y contraseña incorrecta.");
    }

    /**
     * Renueva el token de acceso usando el de refresco.
     *
     * @param tokenRefresco token de refresco vigente
     * @return sesión nueva con un token de acceso reciente
     */
    public SesionDto refrescar(String tokenRefresco) {
        throw new UnsupportedOperationException(
                "La renovación de token se implementara al conectar la persistencia.");
    }

    /**
     * Cierra la sesión actual revocando su token de refresco.
     *
     * @param tokenRefresco token de la sesión a cerrar
     */
    public void cerrarSesion(String tokenRefresco) {
        throw new UnsupportedOperationException(
                "El cierre de sesión se implementara al conectar la persistencia.");
    }

    /**
     * Cierra todas las sesiones del usuario.
     *
     * @param usuarioId identificador del usuario
     * @return número de sesiones revocadas
     */
    public int cerrarTodasLasSesiones(UUID usuarioId) {
        throw new UnsupportedOperationException(
                "El cierre de todas las sesiones se implementara al conectar la persistencia.");
    }

    /**
     * Devuelve el perfil del usuario autenticado.
     *
     * @param usuarioId identificador del usuario
     * @return perfil sin datos sensibles
     */
    public UsuarioAutenticadoDto perfil(UUID usuarioId) {
        throw new UnsupportedOperationException(
                "La consulta de perfil se implementara al conectar la persistencia.");
    }
}
