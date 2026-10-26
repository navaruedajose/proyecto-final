package co.edu.sistematickets.modulos.reportes.dto;

/**
 * Formato de exportación de un reporte.
 *
 * <p>Solo dos formatos, y cada uno tiene su público: el PDF para presentar a
 * un jefe de área que quiere el informe cerrado, y el Excel para que alguien
 * pueda filtrar y sacar sus propias conclusiones. Ofrecer más formatos es
 * trabajo sin retorno.</p>
 */
public enum FormatoReporte {

    /** Documento listo para imprimir. */
    PDF,

    /** Hoja de cálculo para análisis posterior. */
    XLSX
}
