package co.edu.sistematickets.modulos.adjuntos.service;

import co.edu.sistematickets.comun.SolicitudInvalidaExcepcion;
import co.edu.sistematickets.modulos.adjuntos.dto.AdjuntoDto;
import co.edu.sistematickets.modulos.adjuntos.repository.AdjuntoRepositorio;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reglas de negocio de adjuntos (evidencias).
 *
 * <p>La subida de archivos es la superficie de ataque más expuesta de
 * cualquier aplicación web, así que esta clase concentra las comprobaciones.
 * Las cuatro reglas, y el motivo de cada una:</p>
 *
 * <ol>
 *   <li><strong>Tamaño máximo.</strong> Sin límite, un solo archivo puede
 *       llenar el disco y tumbar el servicio para todos. El límite se aplica
 *       antes de leer el contenido a memoria, no después.</li>
 *   <li><strong>Tipo permitido por lista blanca.</strong> Se enumeran los tipos
 *       aceptados y se rechaza todo lo demas. Una lista negra siempre se queda
 *       corta: siempre hay una extensión nueva que nadie previo.</li>
 *   <li><strong>Nombre del archivo saneado.</strong> Se descarta el nombre
 *       original para construir el nombre en disco. Un nombre como
 *       {@code ../../etc/passwd} o {@code archivo.jsp} es la vía clásica para
 *       escapar del directorio o ejecutar código. En disco se guarda un
 *       identificador generado; el nombre original solo se conserva como dato
 *       descriptivo en la base.</li>
 *   <li><strong>Límite de adjuntos por ticket.</strong> Evita que un ticket se
 *       convierta en un vertedero y que una subida masiva sature el
 *       almacenamiento.</li>
 * </ol>
 *
 * <p><strong>Lo que no se comprueba y conviene decir en voz alta.</strong> No
 * hay análisis antivirus. Validar la extensión no garantiza que el contenido
 * sea inocuo: un ejecutable renombrado a {@code .pdf} pasa el filtro. La
 * defensa real está en otro sitio: los archivos se guardan <em>fuera</em> del
 * directorio web y se sirven con {@code Content-Disposition: attachment}, de
 * modo que el navegador los descarga en lugar de interpretarlos. Un antivirus
 * (ClamAV) sería la pieza que falta para cerrar el círculo.</p>
 */
public class AdjuntoServicio {

    /** Tamaño máximo por archivo: 10 MB. */
    public static final long TAMANO_MAXIMO_BYTES = 10L * 1024 * 1024;

    /** Número máximo de adjuntos por ticket. */
    public static final int MAXIMO_ADJUNTOS_POR_TICKET = 10;

    /**
     * Tipos MIME aceptados.
     *
     * <p>Es una lista blanca deliberadamente corta. Incluye imagenes (capturas
     * de pantalla), PDF (informes) y texto plano y comprimidos (logs y trazas),
     * que son las evidencias tipicas de una incidencia técnica.</p>
     */
    public static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "text/csv",
            "application/zip",
            "application/x-zip-compressed"
    );

    /** Extensiones aceptadas, como segunda comprobación del tipo. */
    public static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "pdf", "txt", "csv", "zip", "log"
    );

    private final AdjuntoRepositorio adjuntoRepositorio;

    /**
     * @param adjuntoRepositorio acceso a datos de adjuntos
     */
    public AdjuntoServicio(AdjuntoRepositorio adjuntoRepositorio) {
        this.adjuntoRepositorio = adjuntoRepositorio;
    }

    /**
     * Lista los adjuntos de un ticket.
     *
     * @param ticketId ticket del que se quieren los adjuntos
     * @return lista de adjuntos
     */
    public List<AdjuntoDto> listar(UUID ticketId) {
        return adjuntoRepositorio.listarPorTicket(ticketId);
    }

    /**
     * Sube una o varias evidencias a un ticket.
     *
     * @param ticketId  ticket destino
     * @param archivos  archivos a subir
     * @param usuarioId quien sube
     * @return los adjuntos creados
     * @throws SolicitudInvalidaExcepcion si algún archivo incumple las reglas
     */
    public List<AdjuntoDto> subir(UUID ticketId, List<MultipartFile> archivos, UUID usuarioId) {
        if (archivos == null || archivos.isEmpty()) {
            throw new SolicitudInvalidaExcepcion("Debe adjuntar al menos un archivo.");
        }

        long yaAdjuntos = adjuntoRepositorio.contarPorTicket(ticketId);
        if (yaAdjuntos + archivos.size() > MAXIMO_ADJUNTOS_POR_TICKET) {
            throw new SolicitudInvalidaExcepcion(
                    "Un ticket admite como máximo %d adjuntos y ya tiene %d."
                            .formatted(MAXIMO_ADJUNTOS_POR_TICKET, yaAdjuntos));
        }

        for (MultipartFile archivo : archivos) {
            validarArchivo(archivo);
        }

        throw new UnsupportedOperationException(
                "La subida se completara al conectar el almacenamiento de archivos.");
    }

    /**
     * Elimina un adjunto.
     *
     * @param ticketId   ticket al que pertenece
     * @param adjuntoId  adjunto a eliminar
     * @param usuarioId  quien elimina
     */
    public void eliminar(UUID ticketId, UUID adjuntoId, UUID usuarioId) {
        adjuntoRepositorio.eliminar(adjuntoId);
    }

    /**
     * Valida un archivo antes de aceptarlo.
     *
     * <p>Se comprueban cuatro cosas y en este orden, de la más barata a la más
     * cara: que no este vacío, que no exceda el tamaño, que el tipo este
     * permitido y que la extensión coincida con el tipo. El orden importa
     * porque descartar un archivo de 500 MB antes de mirarlo es mucho más
     * eficiente que analizarlo primero.</p>
     *
     * @param archivo archivo recibido
     * @throws SolicitudInvalidaExcepcion si incumple alguna regla
     */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaExcepcion("El archivo está vacío.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new SolicitudInvalidaExcepcion(
                    "El archivo excede el límite de %d MB.".formatted(TAMANO_MAXIMO_BYTES / (1024 * 1024)));
        }

        String tipo = archivo.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo.toLowerCase())) {
            throw new SolicitudInvalidaExcepcion(
                    "Tipo de archivo no permitido: %s.".formatted(tipo));
        }

        String extension = extraerExtension(archivo.getOriginalFilename());
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new SolicitudInvalidaExcepcion(
                    "Extensión de archivo no permitida: %s.".formatted(extension));
        }
    }

    /**
     * Extrae la extensión de un nombre de archivo, en minusculas.
     *
     * <p>Si el nombre fuera {@code archivo.tar.gz} devuelve {@code gz}, que es
     * lo correcto: la última extensión es la que manda. Devuelve cadena vacía
     * si no hay extensión, y ese caso ya lo rechaza la validación.</p>
     *
     * @param nombre nombre original del archivo; puede ser {@code null}
     * @return extensión sin el punto, en minusculas
     */
    private String extraerExtension(String nombre) {
        if (nombre == null) {
            return "";
        }
        int ultimoPunto = nombre.lastIndexOf('.');
        if (ultimoPunto < 0 || ultimoPunto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(ultimoPunto + 1).toLowerCase();
    }
}
