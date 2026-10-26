package co.edu.sistematickets.modulos.categorias.repository;

import co.edu.sistematickets.modulos.categorias.dto.CategoriaDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de categorías.
 *
 * <p><strong>Estado actual: solo la interfaz.</strong></p>
 */
public interface CategoriaRepositorio {

    /**
     * Lista categorías.
     *
     * @param soloActivas si es {@code true}, omite las desactivadas
     * @return lista de categorías
     */
    List<CategoriaDto> listar(boolean soloActivas);

    /**
     * Busca una categoría por su identificador.
     *
     * @param id identificador de la categoría
     * @return la categoría, o vacío si no existe
     */
    Optional<CategoriaDto> buscarPorId(UUID id);

    /**
     * Busca una categoría por su nombre.
     *
     * <p>La comparación debe ser insensible a mayusculas y espacios sobrantes:
     * «Hardware» y «hardware » son la misma categoría, y aceptar ambas
     * partiria los informes en dos.</p>
     *
     * @param nombre nombre a buscar
     * @return la categoría, o vacío si no existe
     */
    Optional<CategoriaDto> buscarPorNombre(String nombre);

    /**
     * Persiste una categoría nueva.
     *
     * @param categoría datos de la categoría
     * @return la categoría creada
     */
    CategoriaDto guardar(CategoriaDto categoria);

    /**
     * Actualiza una categoría existente.
     *
     * @param id        identificador de la categoría
     * @param categoría datos a persistir
     * @return la categoría actualizada
     */
    CategoriaDto actualizar(UUID id, CategoriaDto categoria);

    /**
     * Desactiva una categoría (borrado lógico).
     *
     * <p>No se borra la fila: los tickets históricos la referencian y
     * eliminarla dejaría sus informes sin categoría.</p>
     *
     * @param id identificador de la categoría
     */
    void desactivar(UUID id);

    /**
     * Cuenta cuantos tickets activos usan una categoría.
     *
     * <p>Se consulta antes de desactivar, para poder avisar de cuantos tickets
     * quedan afectados en lugar de hacerlo en silencio.</p>
     *
     * @param id identificador de la categoría
     * @return número de tickets vigentes en esa categoría
     */
    long contarTicketsActivos(UUID id);
}
