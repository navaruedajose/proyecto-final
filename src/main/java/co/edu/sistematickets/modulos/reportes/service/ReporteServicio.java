package co.edu.sistematickets.modulos.reportes.service;

import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.reportes.dto.FormatoReporte;
import co.edu.sistematickets.modulos.reportes.dto.PanelReporteDto;

import java.time.LocalDate;

/**
 * Reglas de negocio de reportes.
 *
 * <p>Genera el panel de indicadores y los reportes exportables. Dos decisiones
 * que conviene explicar:</p>
 *
 * <ol>
 *   <li><strong>Los reportes se generan en el servidor</strong>, no en el
 *       cliente. Dos razones: el cliente no tendría que descargarse todos los
 *       datos para montar el archivo, y el formato queda garantizado sea cual
 *       sea el navegador. Generar el Excel en el navegador produce archivos
 *       distintos según la versión de la libreria, y el profesor recibiría uno
 *       distinto cada vez.</li>
 *   <li><strong>El rango de fechas tiene límite</strong>. Un reporte de cinco
 *       años sobre una tabla grande puede tardar demasiado y bloquear una
 *       conexión de base de datos. Conviene acotarlo por defecto (por ejemplo,
 *       los ultimos 30 días) y como máximo un intervalo razonable.</li>
 * </ol>
 *
 * <p>Solo los administradores pueden exportar: los informes agregados revelan
 * el volumen de trabajo y el rendimiento de cada agente.</p>
 */
public class ReporteServicio {

    /**
     * Devuelve los indicadores del panel.
     *
     * @param desde fecha inicial del rango; puede ser {@code null}
     * @param hasta fecha final del rango; puede ser {@code null}
     * @param rol   rol de quien consulta
     * @return indicadores agregados
     */
    public PanelReporteDto panel(LocalDate desde, LocalDate hasta, RolUsuario rol) {
        throw new UnsupportedOperationException(
                "El panel se implementara al conectar la base de datos, leyendo las vistas de resumen.");
    }

    /**
     * Genera un reporte exportable.
     *
     * <p>Devuelve {@code byte[]} y no un objeto: el resultado final es el
     * archivo en si. El controlador lo enviara tal cual con las cabeceras
     * adecuadas.</p>
     *
     * @param desde   fecha inicial del rango
     * @param hasta   fecha final del rango
     * @param formato formato de salida, PDF o XLSX
     * @param rol     rol de quien solicita
     * @return el archivo generado, listo para descargar
     */
    public byte[] exportar(LocalDate desde, LocalDate hasta, FormatoReporte formato, RolUsuario rol) {
        throw new UnsupportedOperationException(
                "La exportación se implementara al conectar la base de datos. "
                        + "Formato solicitado: " + formato);
    }

    /**
     * Elimina datos de prueba de la base de datos.
     *
     * <p>Es utilidad de mantenimiento, no funcionalidad de negocio. Existe
     * porque las pruebas de carga crean miles de tickets: reiniciar el volumen
     * de Docker para limpiarlos es desproporcionado, y este método permite
     * borrar solo lo reciente sin tocar los datos de demostración.</p>
     *
     * @param horasAntiguedad borra los tickets creados en las ultimas horas
     * @param rol             rol de quien solicita
     * @return número de tickets eliminados
     */
    public long limpiarDatosDePrueba(int horasAntiguedad, RolUsuario rol) {
        throw new UnsupportedOperationException(
                "La limpieza se implementara al conectar la base de datos.");
    }
}
