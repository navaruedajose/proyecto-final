package co.edu.sistematickets.modulos.categorias.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos para crear o modificar una categoría.
 *
 * <p>El campo {@code slaHoras} es el más importante de este DTO: define
 * cuantas horas tiene el equipo para resolver un ticket de esta categoría, y
 * de el depende la fecha límite que calcula un trigger de base de datos.</p>
 *
 * <p>El rango de 1 a 720 horas (un mes) replica la restricción
 * {@code ck_categorias_sla_positivo} del esquema. El límite superior no es
 * arbitrario: un SLA de más de un mes dejaría de ser un compromiso de servicio
 * y no aportaria nada a los informes de cumplimiento.</p>
 *
 * @param nombre      nombre de la categoría; debe ser único
 * @param descripción texto que explica que incidencias entran aquí
 * @param slaHoras    horas maximas comprometidas para resolver; entre 1 y 720
 * @param activo      si la categoría está disponible para nuevos tickets
 */
public record CategoriaDto(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres.")
        String nombre,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres.")
        String descripcion,

        @NotNull(message = "El SLA en horas es obligatorio.")
        @Min(value = 1, message = "El SLA debe ser de al menos 1 hora.")
        @Max(value = 720, message = "El SLA no puede exceder 720 horas (30 días).")
        Integer slaHoras,

        Boolean activo,

        /** Solo en la respuesta: no se envía al crear. */
        UUID id
) {
}
