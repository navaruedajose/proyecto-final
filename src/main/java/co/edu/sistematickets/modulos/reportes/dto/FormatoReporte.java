package co.edu.sistematickets.modulos.reportes.dto;

/**
 * Formato de exportacion de un reporte.
 *
 * <p>Solo dos formatos, y cada uno tiene su publico: el PDF para presentar a
 * un jefe de area que quiere el informe cerrado, y el Excel para que alguien
 * pueda filtrar y sacar sus propias conclusiones. Ofrecer mas formatos es
 * trabajo sin retorno.</p>
 */
public enum FormatoReporte {

    /** Documento listo para imprimir. */
    PDF,

    /** Hoja de calculo para analisis posterior. */
    XLSX
}
