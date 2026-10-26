package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de la aplicación.
 *
 * <p>Todas las excepciones de negocio heredan de aquí y <strong>cada una sabe
 * que código HTTP le corresponde</strong>. Esto tiene una consecuencia
 * importante: el manejador global de errores no necesita una cadena de
 * {@code if} para decidir el estado, solo lee {@link #getEstadoHttp()}. Añadir
 * un error nuevo no obliga a tocar el manejador (abierto a extensión, cerrado
 * a modificación).</p>
 */
public abstract class ExcepcionAplicacion extends RuntimeException {

    private final HttpStatus estadoHttp;
    private final String codigo;

    /**
     * @param mensaje    descripción legible del problema
     * @param estadoHttp código HTTP que debe devolver la API
     * @param código     código estable de error para el cliente
     */
    protected ExcepcionAplicacion(String mensaje, HttpStatus estadoHttp, String codigo) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }

    /**
     * @return código HTTP que corresponde a este error
     */
    public HttpStatus getEstadoHttp() {
        return estadoHttp;
    }

    /**
     * @return código estable de error, independiente del texto del mensaje
     */
    public String getCodigo() {
        return codigo;
    }
}
