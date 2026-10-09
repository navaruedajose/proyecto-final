package co.edu.sistematickets.modulos.usuarios.service;

import co.edu.sistematickets.comun.NoEncontradoExcepcion;
import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.comun.SinPermisoExcepcion;
import co.edu.sistematickets.comun.SolicitudInvalidaExcepcion;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.usuarios.dto.ActualizarUsuarioDto;
import co.edu.sistematickets.modulos.usuarios.dto.CambiarContrasenaDto;
import co.edu.sistematickets.modulos.usuarios.dto.UsuarioDto;
import co.edu.sistematickets.modulos.usuarios.repository.UsuarioRepositorio;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de gestion de usuarios.
 *
 * <p>Decisiones que aplica y conviene poder explicar:</p>
 *
 * <ol>
 *   <li><strong>Un usuario solo se edita a si mismo</strong>, salvo el
 *       administrador. Y un no-administrador no puede tocar su propio rol ni
 *       su estado de activacion: si pudiera, cualquiera se ascenderia
 *       mandando {@code "rol": "ADMINISTRADOR"}.</li>
 *   <li><strong>Un administrador no puede desactivarse a si mismo.</strong> Es
 *       la salvaguarda clasica: evita que el sistema se quede sin ningun
 *       administrador activo y nadie pueda volver a entrar. Es la razon de que
 *       el proyecto incluya ademas un script de rescate.</li>
 *   <li><strong>Cambiar la contrasena revoca todas las sesiones.</strong> Con
 *       el token antiguo ya no se entra.</li>
 * </ol>
 */
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    /**
     * @param usuarioRepositorio acceso a datos de usuarios
     */
    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * Lista usuarios con filtros y paginacion.
     *
     * @param rol      filtro por rol; puede ser {@code null}
     * @param activo   filtro por estado; puede ser {@code null}
     * @param busqueda texto libre; puede ser {@code null}
     * @param pagina   numero de pagina
     * @param limite   tamano de pagina
     * @return pagina de usuarios y metadatos
     */
    public RespuestaApi<List<UsuarioDto>> listar(RolUsuario rol, Boolean activo, String busqueda,
                                                 int pagina, int limite) {
        List<UsuarioDto> contenido = usuarioRepositorio.listar(rol, activo, busqueda, pagina, limite);
        long total = usuarioRepositorio.contar(rol, activo, busqueda);
        return RespuestaApi.paginado(contenido, pagina, limite, total);
    }

    /**
     * Devuelve un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return el usuario
     * @throws NoEncontradoExcepcion si no existe
     */
    public UsuarioDto obtener(UUID id) {
        return usuarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoExcepcion("Usuario", id));
    }

    /**
     * Devuelve los agentes ordenados por carga de trabajo.
     *
     * @return agentes de menor a mayor carga
     */
    public List<UsuarioDto> listarAgentes() {
        return usuarioRepositorio.listarAgentesPorCarga();
    }

    /**
     * Actualiza un usuario.
     *
     * <p>Comprueba dos cosas antes de persistir: que quien edita tenga derecho
     * a hacerlo, y que si no es administrador no este intentando cambiar el
     * rol o el estado de activacion. La segunda comprobacion es la importante;
     * sin ella, la primera no serviria de mucho.</p>
     *
     * @param id          usuario a editar
     * @param cambios     campos a modificar
     * @param usuarioId   usuario que realiza la edicion
     * @param rolEditor   rol de quien edita
     * @return el usuario actualizado
     */
    public UsuarioDto actualizar(UUID id, ActualizarUsuarioDto cambios, UUID usuarioId, RolUsuario rolEditor) {
        obtener(id);

        if (!usuarioId.equals(id) && rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion("Solo puede modificar su propio perfil.");
        }
        if (cambios.estaVacio()) {
            throw new SolicitudInvalidaExcepcion("Debe indicar al menos un campo a actualizar.");
        }
        if (rolEditor != RolUsuario.ADMINISTRADOR && (cambios.rol() != null || cambios.activo() != null)) {
            throw new SinPermisoExcepcion(
                    "Solo un administrador puede cambiar el rol o el estado de una cuenta.");
        }

        throw new UnsupportedOperationException(
                "La actualizacion se completara al conectar la persistencia.");
    }

    /**
     * Desactiva un usuario (borrado logico).
     *
     * <p>Dos salvaguardas antes de desactivar: solo un administrador puede
     * hacerlo, y no puede desactivarse a si mismo. La segunda evita que el
     * sistema se quede sin ningun administrador activo, que seria un callejon
     * sin salida.</p>
     *
     * @param id        usuario a desactivar
     * @param usuarioId quien realiza la operacion
     * @param rolEditor rol de quien la realiza
     */
    public void desactivar(UUID id, UUID usuarioId, RolUsuario rolEditor) {
        obtener(id);

        if (rolEditor != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion("Solo un administrador puede desactivar cuentas.");
        }
        if (usuarioId != null && usuarioId.equals(id)) {
            throw new SolicitudInvalidaExcepcion(
                    "No puede desactivar su propia cuenta: el sistema quedaria sin administrador.");
        }
        usuarioRepositorio.desactivar(id);
    }

    /**
     * Cambia la contrasena del propio usuario.
     *
     * <p>Exige la contrasena actual y, al terminar, revoca todas las sesiones.
     * El orden importa: primero se comprueba la contrasena actual, y solo si
     * es correcta se cambia y se revoca.</p>
     *
     * @param usuarioId identificador del usuario
     * @param datos     contrasena actual y nueva
     */
    public void cambiarContrasena(UUID usuarioId, CambiarContrasenaDto datos) {
        obtener(usuarioId);
        throw new UnsupportedOperationException(
                "El cambio de contrasena se implementara al conectar la persistencia. "
                        + "Recordar: verificar la actual y revocar todas las sesiones.");
    }
}
