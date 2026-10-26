package co.edu.sistematickets.comun;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Contrato único de respuesta de la API.
 *
 * <p>Todos los endpoints devuelven la <strong>misma</strong> forma, de modo que
 * el frontend escribe un solo interceptor y no tiene que adivinar la estructura
 * endpoint por endpoint:</p>
 *
 * <pre>
 *   éxito:   { "éxito": true,  "datos": &lt;...&gt;, "mensaje": "...", "meta": {...} }
 *   error:   { "éxito": false, "mensaje": "...", "código": "...", "detalles": [...] }
 * </pre>
 *
 * <p>Se implementa como un {@code record} porque es un objeto inmutable de
 * transporte: se construye una vez y no se modifica. Los campos opcionales se
 * omiten del JSON con {@link JsonInclude} para no ensuciar la respuesta con
 * {@code null} innecesarios.</p>
 *
 * @param éxito    indica si la operación tuvo éxito
 * @param datos    carga útil; {@code null} en las respuestas de error
 * @param mensaje  texto legible para el usuario; {@code null} si no aporta nada
 * @param meta     información adicional, normalmente la paginación
 * @param código   código estable de error, para que el cliente lo programe
 * @param detalles lista de errores por campo, tipicamente de validación
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaApi<T>(
        boolean exito,
        T datos,
        String mensaje,
        Map<String, Object> meta,
        String codigo,
        java.util.List<DetalleError> detalles
) {

    /**
     * Respuesta exitosa con carga útil.
     *
     * @param datos contenido a devolver
     * @param <T>   tipo de la carga útil
     * @return respuesta con {@code éxito = true}
     */
    public static <T> RespuestaApi<T> ok(T datos) {
        return new RespuestaApi<>(true, datos, null, null, null, null);
    }

    /**
     * Respuesta exitosa con carga útil y mensaje.
     *
     * @param datos   contenido a devolver
     * @param mensaje texto legible para el usuario
     * @param <T>     tipo de la carga útil
     * @return respuesta con {@code éxito = true}
     */
    public static <T> RespuestaApi<T> ok(T datos, String mensaje) {
        return new RespuestaApi<>(true, datos, mensaje, null, null, null);
    }

    /**
     * Respuesta creada (HTTP 201).
     *
     * @param datos   recurso recién creado
     * @param mensaje texto legible para el usuario
     * @param <T>     tipo de la carga útil
     * @return respuesta con {@code éxito = true}
     */
    public static <T> RespuestaApi<T> creado(T datos, String mensaje) {
        return new RespuestaApi<>(true, datos, mensaje, null, null, null);
    }

    /**
     * Respuesta paginada estándar.
     *
     * <p>La paginación viaja en {@code meta} y nunca dentro de {@code datos},
     * para que el cliente pueda tratar {@code datos} siempre como una lista
     * del mismo tipo, tenga o no paginación.</p>
     *
     * @param contenido  elementos de la página actual
     * @param página     número de página, empezando en 1
     * @param límite     tamaño de página solicitado
     * @param total      total de elementos que cumplen el filtro
     * @param <T>        tipo de los elementos
     * @return respuesta con el bloque {@code meta.paginación} relleno
     */
    public static <T> RespuestaApi<java.util.List<T>> paginado(
            java.util.List<T> contenido, int pagina, int limite, long total) {

        int totalPaginas = limite > 0 ? (int) Math.ceil((double) total / limite) : 0;
        Map<String, Object> paginacion = Map.of(
                "pagina", pagina,
                "limite", limite,
                "total", total,
                "totalPaginas", totalPaginas,
                "tieneSiguiente", (long) pagina * limite < total,
                "tieneAnterior", pagina > 1
        );
        return new RespuestaApi<>(true, contenido, null, Map.of("paginacion", paginacion), null, null);
    }

    /**
     * Respuesta de error.
     *
     * @param mensaje  descripción legible del problema
     * @param código   código estable, por ejemplo {@code NO_ENCONTRADO}
     * @param detalles errores por campo; puede ser {@code null}
     * @param <T>      tipo de la carga útil (siempre {@code null} en errores)
     * @return respuesta con {@code éxito = false}
     */
    public static <T> RespuestaApi<T> error(String mensaje, String codigo, java.util.List<DetalleError> detalles) {
        return new RespuestaApi<>(false, null, mensaje, null, codigo, detalles);
    }

    /**
     * Error de validación, con el detalle campo a campo.
     *
     * @param mensaje  resumen legible
     * @param detalles lista de campos invalidos y su motivo
     * @param <T>      tipo de la carga útil
     * @return respuesta con código {@code VALIDACIÓN}
     */
    public static <T> RespuestaApi<T> validacion(String mensaje, java.util.List<DetalleError> detalles) {
        return new RespuestaApi<>(false, null, mensaje, null, "VALIDACION", detalles);
    }

    /**
     * Marca temporal de generación de la respuesta.
     *
     * <p>Se calcula al serializar y no se guarda como campo, para que el
     * registro siga siendo comparable por igualdad sin depender del reloj.</p>
     *
     * @return instante actual
     */
    public Instant generadoEn() {
        return Instant.now();
    }
}
