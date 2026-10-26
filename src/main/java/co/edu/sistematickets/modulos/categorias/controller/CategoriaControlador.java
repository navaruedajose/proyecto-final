package co.edu.sistematickets.modulos.categorias.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.categorias.dto.CategoriaDto;
import co.edu.sistematickets.modulos.categorias.service.CategoriaServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints REST de categorías.
 *
 * <p>La lectura está abierta a cualquier usuario autenticado porque el
 * formulario de creación de tickets necesita la lista. La escritura queda para
 * administradores.</p>
 */
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    /**
     * @param categoriaServicio servicio de categorías
     */
    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    /**
     * Lista categorías.
     *
     * @param soloActivas si es {@code true}, solo las habilitadas
     * @return lista de categorías
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<CategoriaDto>>> listar(
            @RequestParam(defaultValue = "false") boolean soloActivas) {

        return ResponseEntity.ok(RespuestaApi.ok(categoriaServicio.listar(soloActivas)));
    }

    /**
     * Devuelve una categoría por su identificador.
     *
     * @param id identificador de la categoría
     * @return la categoría
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<CategoriaDto>> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(RespuestaApi.ok(categoriaServicio.obtener(id)));
    }

    /**
     * Crea una categoría.
     *
     * @param datos datos de la categoría
     * @return la categoría creada
     */
    @PostMapping
    public ResponseEntity<RespuestaApi<CategoriaDto>> crear(@Valid @RequestBody CategoriaDto datos) {
        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        CategoriaDto creada = categoriaServicio.crear(datos, rol);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.creado(creada, "Categoría creada correctamente."));
    }

    /**
     * Actualiza una categoría.
     *
     * @param id    identificador de la categoría
     * @param datos datos a persistir
     * @return la categoría actualizada
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RespuestaApi<CategoriaDto>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CategoriaDto datos) {

        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        return ResponseEntity.ok(RespuestaApi.ok(
                categoriaServicio.actualizar(id, datos, rol),
                "Categoría actualizada correctamente."));
    }

    /**
     * Desactiva una categoría.
     *
     * <p>Devuelve en {@code meta} cuantos tickets activos quedaron afectados.
     * Es información útil: desactivar una categoría con incidencias abiertas
     * deja esos tickets sin SLA aplicable, y el cliente puede avisar al
     * usuario en lugar de hacerlo a ciegas.</p>
     *
     * @param id identificador de la categoría
     * @return respuesta sin cuerpo de datos, con el aviso en {@code meta}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> desactivar(@PathVariable UUID id) {
        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        long afectados = categoriaServicio.desactivar(id, rol);

        return ResponseEntity.ok(new RespuestaApi<>(
                true, null,
                "Categoría desactivada. Tickets activos afectados: " + afectados + ".",
                Map.of("ticketsAfectados", afectados),
                null, null));
    }
}
