package co.edu.sistematickets.config;

import co.edu.sistematickets.comun.DetalleError;
import co.edu.sistematickets.comun.ExcepcionAplicacion;
import co.edu.sistematickets.comun.RespuestaApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Manejador global de errores.
 *
 * <p>Concentra en un solo sitio la traduccion de excepcion a respuesta HTTP.
 * Sin esto, cada controlador tendria que envolver sus llamadas en
 * {@code try/catch} y el formato de error acabaria siendo distinto en cada
 * endpoint.</p>
 *
 * <p><strong>Por que esto cumple el principio de abierto/cerrado:</strong> cada
 * excepcion de negocio ya sabe su codigo HTTP (hereda de
 * {@link ExcepcionAplicacion}). El manejador no necesita una cadena de
 * {@code if} para averiguarlo: hay un metodo para la familia entera. Anadir un
 * error nuevo no obliga a tocar esta clase.</p>
 *
 * <p><strong>Sobre el mensaje de error 500.</strong> Cuando falla algo no
 * previsto, la respuesta al cliente es generica y el detalle completo va al
 * registro del servidor. Devolver el mensaje de la excepcion interna revelaria
 * nombres de clases, rutas del servidor y a veces fragmentos de consultas SQL:
 * informacion muy util para quien busca un fallo que explotar.</p>
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    /**
     * Traduce cualquier excepcion de negocio a su respuesta.
     *
     * <p>Un unico metodo cubre las cinco excepciones del proyecto
     * ({@code NoEncontrado}, {@code SolicitudInvalida},
     * {@code ConflictoEstado}, {@code SinPermiso},
     * {@code RecursoDuplicado}) porque todas exponen el codigo HTTP que les
     * corresponde.</p>
     *
     * @param excepcion excepcion de negocio
     * @return respuesta con el estado y el codigo propios de la excepcion
     */
    @ExceptionHandler(ExcepcionAplicacion.class)
    public ResponseEntity<RespuestaApi<Void>> manejarExcepcionDeAplicacion(ExcepcionAplicacion excepcion) {
        return ResponseEntity
                .status(excepcion.getEstadoHttp())
                .body(RespuestaApi.error(excepcion.getMessage(), excepcion.getCodigo(), null));
    }

    /**
     * Traduce errores de validacion de Bean Validation.
     *
     * <p>Devuelve {@code 400} con la lista completa de campos invalidos, no
     * solo el primero. Si el formulario tiene tres problemas, es mejor
     * decirlos todos de una vez que obligar al usuario a corregir de uno en
     * uno descubriendo el siguiente en cada intento.</p>
     *
     * @param excepcion error de validacion
     * @return respuesta con el detalle por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarValidacion(MethodArgumentNotValidException excepcion) {
        List<DetalleError> detalles = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> new DetalleError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespuestaApi.validacion("Los datos enviados no son validos.", detalles));
    }

    /**
     * Traduce un cuerpo de peticion ilegible.
     *
     * <p>Ocurre cuando el JSON esta mal formado o cuando un valor no encaja en
     * el tipo esperado (por ejemplo, {@code "estado": "INVENTADO"} frente a la
     * enumeracion). El mensaje por defecto de Spring revela la ruta de las
     * clases internas, asi que se sustituye por uno propio.</p>
     *
     * @param excepcion error de lectura del cuerpo
     * @return respuesta con un mensaje generico y util
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarCuerpoIlegible(HttpMessageNotReadableException excepcion) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespuestaApi.error(
                        "El cuerpo de la peticion esta mal formado o contiene un valor no permitido.",
                        "CUERPO_ILEGIBLE", null));
    }

    /**
     * Traduce un parametro de ruta o consulta con el tipo equivocado.
     *
     * <p>El caso tipico es un {@code /tickets/abc} donde se espera un
     * identificador. Merece su propio manejador porque el mensaje por defecto
     * de Spring es una volcado de tipos internos que no ayuda a nadie.</p>
     *
     * @param excepcion error de conversion de tipo
     * @return respuesta indicando el parametro mal formado
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarTipoIncorrecto(MethodArgumentTypeMismatchException excepcion) {
        String mensaje = "El parametro '%s' tiene un formato no valido.".formatted(excepcion.getName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespuestaApi.error(mensaje, "PARAMETRO_INVALIDO", null));
    }

    /**
     * Red de seguridad para cualquier error no previsto.
     *
     * <p>Captura lo que no encaja en ningun manejador anterior y devuelve un
     * {@code 500} con un mensaje neutro. El detalle real se registra en el
     * servidor, nunca se envia al cliente.</p>
     *
     * @param excepcion excepcion no controlada
     * @return respuesta genérica de error interno
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi<Void>> manejarErrorNoControlado(Exception excepcion) {
        // Aqui iria el registro: logger.error("Error no controlado", excepcion);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespuestaApi.error(
                        "Ocurrio un error interno. El equipo tecnico ha sido notificado.",
                        "ERROR_INTERNO", null));
    }
}
