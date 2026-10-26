package co.edu.sistematickets.modulos.reportes.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.reportes.dto.FormatoReporte;
import co.edu.sistematickets.modulos.reportes.dto.PanelReporteDto;
import co.edu.sistematickets.modulos.reportes.service.ReporteServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoints REST de reportes.
 *
 * <p>Un solo endpoint para los dos formatos, elegidos por el parámetro
 * {@code formato}. Se separan en rutas distintas solo si las opciones de cada
 * uno divergieran de verdad; hoy comparten exactamente los mismos filtros, así
 * que duplicar la ruta obligaría a mantener dos firmas identicas.</p>
 *
 * <p><strong>Ruta exacta:</strong> {@code /reportes/tickets?días=30&formato=pdf}.
 * Conviene tenerla presente porque es fácil equivocarse y escribir
 * {@code /reportes/pdf}.</p>
 */
@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    /**
     * @param reporteServicio servicio de reportes
     */
    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    /**
     * Devuelve los indicadores del panel en JSON.
     *
     * @param desde fecha inicial; puede ser {@code null}
     * @param hasta fecha final; puede ser {@code null}
     * @return indicadores agregados
     */
    @GetMapping("/dashboard")
    public ResponseEntity<RespuestaApi<PanelReporteDto>> panel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        return ResponseEntity.ok(RespuestaApi.ok(reporteServicio.panel(desde, hasta, rol)));
    }

    /**
     * Descarga el reporte de tickets en PDF o Excel.
     *
     * <p>Devuelve el archivo directo, sin envolverlo en el contrato JSON. Las
     * cabeceras que hacen que la descarga funcione bien son tres:</p>
     * <ul>
     *   <li>{@code Content-Type} — el tipo real del archivo. Debe ser el
     *       correcto ({@code application/pdf} o el tipo de Excel), no
     *       {@code application/octet-stream}: así el sistema operativo abre el
     *       archivo con la aplicación adecuada al descargarlo.</li>
     *   <li>{@code Content-Disposition: attachment} — fuerza la descarga en
     *       lugar de intentar abrirlo en una pestana.</li>
     *   <li>{@code Content-Length} — lo calcula Spring a partir del arreglo de
     *       bytes, y permite al navegador mostrar el progreso.</li>
     * </ul>
     *
     * @param días    ventana temporal en días hacia atrás
     * @param formato {@code pdf} o {@code xlsx}
     * @return el archivo listo para descargar
     */
    @GetMapping("/tickets")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(defaultValue = "30") int dias,
            @RequestParam(defaultValue = "pdf") FormatoReporte formato) {

        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusDays(dias);

        byte[] contenido = reporteServicio.exportar(desde, hasta, formato, rol);

        String nombreArchivo = "reporte-tickets." + formato.name().toLowerCase();
        MediaType tipoContenido = (formato == FormatoReporte.PDF)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .contentType(tipoContenido)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }

    /**
     * Elimina datos de prueba de la base de datos.
     *
     * @param horas antiguedad en horas de lo que se va a borrar
     * @return número de tickets eliminados
     */
    @PostMapping("/limpiar")
    public ResponseEntity<RespuestaApi<Long>> limpiar(@RequestParam(defaultValue = "24") int horas) {
        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        long eliminados = reporteServicio.limpiarDatosDePrueba(horas, rol);

        return ResponseEntity.ok(RespuestaApi.ok(eliminados,
                "Datos de prueba eliminados: " + eliminados + " tickets."));
    }

    /**
     * Constante de compatibilidad para la documentación de la API.
     *
     * <p>Se deja escrita la ruta para que quede en el código la advertencia de
     * que <strong>no</strong> existe un {@code /reportes/pdf} ni un
     * {@code /reportes/xlsx}: el formato es un parámetro, no un segmento de
     * ruta.</p>
     */
    public static final String RUTA_CORRECTA = "/api/v1/reportes/tickets?dias=30&formato=pdf|xlsx";
}
