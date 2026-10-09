package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * Excepcion base de la aplicacion.
 *
 * <p>Todas las excepciones de negocio heredan de aqui y <strong>cada una sabe
 * que codigo HTTP le corresponde</strong>. Esto tiene una consecuencia
 * importante: el manejador global de errores no necesita una cadena de
 * {@code if} para decidir el estado, solo lee {@link #getEstadoHttp()}. Anadir
 * un error nuevo no obliga a tocar el manejador (abierto a extension, cerrado
 * a modificacion).</p>
 */
public abstract class ExcepcionAplicacion extends RuntimeException {

    private final HttpStatus estadoHttp;
    private final String codigo;

    /**
     * @param mensaje    descripcion legible del problema
     * @param estadoHttp codigo HTTP que debe devolver la API
     * @param codigo     codigo estable de error para el cliente
     */
    protected ExcepcionAplicacion(String mensaje, HttpStatus estadoHttp, String codigo) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }

    /**
     * @return codigo HTTP que corresponde a este error
     */
    public HttpStatus getEstadoHttp() {
        return estadoHttp;
    }

    /**
     * @return codigo estable de error, independiente del texto del mensaje
     */
    public String getCodigo() {
        return codigo;
    }
}
