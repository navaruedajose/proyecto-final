package co.edu.sistematickets.modulos.categorias.service;

import co.edu.sistematickets.comun.NoEncontradoExcepcion;
import co.edu.sistematickets.comun.RecursoDuplicadoExcepcion;
import co.edu.sistematickets.comun.SinPermisoExcepcion;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.categorias.dto.CategoriaDto;
import co.edu.sistematickets.modulos.categorias.repository.CategoriaRepositorio;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de categorías.
 *
 * <p>Parece un módulo de mantenimiento sencillo, pero tiene dos reglas que
 * importan:</p>
 *
 * <ol>
 *   <li><strong>El nombre es único.</strong> Del nombre depende el SLA, y dos
 *       categorías que se llamen casi igual parten los informes en dos. Se
 *       comprueba antes de insertar para devolver un error claro en lugar de
 *       dejar que salte la restricción de la base.</li>
 *   <li><strong>Desactivar no borra.</strong> Se comprueba cuantos tickets
 *       activos usan la categoría y se informa, porque desactivar una
 *       categoría con incidencias abiertas deja esos tickets sin SLA
 *       aplicable.</li>
 * </ol>
 *
 * <p>Solo un administrador puede crear, modificar o desactivar categorías. Que
 * un agente pudiera cambiarlas alteraria los SLA de todo el equipo, que no es
 * una decisión suya.</p>
 */
public class CategoriaServicio {

    private final CategoriaRepositorio categoriaRepositorio;

    /**
     * @param categoriaRepositorio acceso a datos de categorías
     */
    public CategoriaServicio(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    /**
     * Lista categorías.
     *
     * <p>La consulta está abierta a cualquier usuario autenticado: el
     * formulario de creación de ticket necesita mostrar las categorías
     * disponibles para que el usuario elija.</p>
     *
     * @param soloActivas si es {@code true}, omite las desactivadas
     * @return lista de categorías
     */
    public List<CategoriaDto> listar(boolean soloActivas) {
        return categoriaRepositorio.listar(soloActivas);
    }

    /**
     * Devuelve una categoría por su identificador.
     *
     * @param id identificador de la categoría
     * @return la categoría
     * @throws NoEncontradoExcepcion si no existe
     */
    public CategoriaDto obtener(UUID id) {
        return categoriaRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoExcepcion("Categoria", id));
    }

    /**
     * Crea una categoría.
     *
     * @param datos datos de la categoría
     * @param rol   rol de quien realiza la operación
     * @return la categoría creada
     * @throws SinPermisoExcepcion       si no es administrador
     * @throws RecursoDuplicadoExcepcion si el nombre ya está en uso
     */
    public CategoriaDto crear(CategoriaDto datos, RolUsuario rol) {
        exigirAdministrador(rol);

        categoriaRepositorio.buscarPorNombre(datos.nombre()).ifPresent(existente -> {
            throw new RecursoDuplicadoExcepcion("categoria", "nombre", datos.nombre());
        });

        throw new UnsupportedOperationException(
                "La creación se completara al conectar la persistencia.");
    }

    /**
     * Actualiza una categoría.
     *
     * <p>Al cambiar el SLA, los tickets ya abiertos de esa categoría mantienen
     * la fecha límite que se les calculó al crearse. Es una decisión
     * deliberada: recalcularla hacia atrás movería incumplimientos históricos
     * y falsearia los informes de meses ya cerrados.</p>
     *
     * @param id    identificador de la categoría
     * @param datos datos a persistir
     * @param rol   rol de quien realiza la operación
     * @return la categoría actualizada
     */
    public CategoriaDto actualizar(UUID id, CategoriaDto datos, RolUsuario rol) {
        exigirAdministrador(rol);
        obtener(id);

        categoriaRepositorio.buscarPorNombre(datos.nombre()).ifPresent(existente -> {
            if (!existente.id().equals(id)) {
                throw new RecursoDuplicadoExcepcion("categoria", "nombre", datos.nombre());
            }
        });

        throw new UnsupportedOperationException(
                "La actualización se completara al conectar la persistencia.");
    }

    /**
     * Desactiva una categoría.
     *
     * @param id  identificador de la categoría
     * @param rol rol de quien realiza la operación
     * @return número de tickets activos que quedaron afectados
     */
    public long desactivar(UUID id, RolUsuario rol) {
        exigirAdministrador(rol);
        obtener(id);

        long afectados = categoriaRepositorio.contarTicketsActivos(id);
        categoriaRepositorio.desactivar(id);
        return afectados;
    }

    /**
     * Comprueba que quien opera sea administrador.
     *
     * @param rol rol del usuario
     * @throws SinPermisoExcepcion si no lo es
     */
    private void exigirAdministrador(RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion("Solo un administrador puede gestionar las categorías.");
        }
    }
}
