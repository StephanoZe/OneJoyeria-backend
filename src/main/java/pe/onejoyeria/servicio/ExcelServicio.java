package pe.onejoyeria.servicio;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.onejoyeria.dto.CategoriaResumenDto;
import pe.onejoyeria.dto.ProductoDto;
import pe.onejoyeria.modelo.MovimientoStock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera los reportes en Excel: entradas y salidas, e inventario valorizado. */
@Service
@Transactional(readOnly = true)
public class ExcelServicio {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InventarioServicio inventario;

    public ExcelServicio(InventarioServicio inventario) {
        this.inventario = inventario;
    }

    /** Excel de entradas y salidas del periodo, con totales al final. */
    public byte[] movimientos(LocalDate desde, LocalDate hasta, Long categoriaId) throws IOException {
        List<MovimientoStock> movimientos = inventario.filtrarMovimientos(desde, hasta, categoriaId, null);
        String periodo = "Del " + (desde == null ? "inicio" : desde.format(FECHA))
                + " al " + (hasta == null ? "día de hoy" : hasta.format(FECHA));

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(wb);
            Sheet h = wb.createSheet("Entradas y salidas");
            int fila = titulo(h, e, "One Joyería — Entradas y salidas", periodo, 9);
            fila = encabezado(h, e, fila, "Fecha", "Código", "Pieza", "Tipo de joya", "Tipo",
                    "Cantidad", "Stock anterior", "Stock nuevo", "Responsable", "Observaciones");
            int primera = fila;
            int entradas = 0, salidas = 0;
            for (MovimientoStock m : movimientos) {
                Row r = h.createRow(fila++);
                boolean esEntrada = MovimientoStock.ENTRADA.equals(m.getTipoMovimiento());
                if (esEntrada) entradas += m.getCantidad(); else salidas += m.getCantidad();
                texto(r, 0, m.getFechaMovimiento().format(FECHA_HORA), e.celda);
                texto(r, 1, m.getProducto().getCodigo(), e.celda);
                texto(r, 2, m.getProducto().getNombre(), e.celda);
                texto(r, 3, m.getProducto().getCategoria().getNombre(), e.celda);
                texto(r, 4, m.getTipoMovimiento(), esEntrada ? e.disponible : e.bajo);
                entero(r, 5, m.getCantidad(), e.entero);
                entero(r, 6, m.getStockAnterior(), e.entero);
                entero(r, 7, m.getStockNuevo(), e.entero);
                texto(r, 8, m.getResponsable() == null ? "—" : m.getResponsable(), e.celda);
                texto(r, 9, m.getObservaciones() == null ? "" : m.getObservaciones(), e.celda);
            }
            if (movimientos.isEmpty()) {
                texto(h.createRow(fila++), 0, "No hay entradas ni salidas en este periodo.", e.celda);
            }
            fila++;
            Row rE = h.createRow(fila++);
            texto(rE, 4, "Total entradas", e.total);
            entero(rE, 5, entradas, e.totalEntero);
            Row rS = h.createRow(fila++);
            texto(rS, 4, "Total salidas", e.total);
            entero(rS, 5, salidas, e.totalEntero);
            Row rM = h.createRow(fila);
            texto(rM, 4, "Movimientos", e.total);
            entero(rM, 5, movimientos.size(), e.totalEntero);

            anchos(h, 17, 11, 30, 13, 14, 10, 14, 12, 20, 32);
            h.createFreezePane(0, primera);
            return escribir(wb, salida);
        }
    }

    /** Excel del inventario valorizado (stock actual a precio de venta) y su resumen por tipo de joya. */
    public byte[] inventarioValorizado(Long categoriaId) throws IOException {
        List<ProductoDto> piezas = inventario.listar(null, categoriaId, null);
        List<CategoriaResumenDto> resumen = inventario.resumenPorCategoria(categoriaId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(wb);

            // ── Hoja 1: Inventario ──
            Sheet h1 = wb.createSheet("Inventario valorizado");
            int fila = titulo(h1, e, "One Joyería — Inventario valorizado",
                    "Generado el " + LocalDateTime.now().format(FECHA_HORA), 9);
            fila = encabezado(h1, e, fila, "Código", "Pieza", "Tipo de joya", "Material", "Proveedor",
                    "Precio (S/)", "Stock", "Mínimo", "Valor (S/)", "Estado");
            int primera = fila;
            for (ProductoDto p : piezas) {
                Row r = h1.createRow(fila++);
                texto(r, 0, p.codigo(), e.celda);
                texto(r, 1, p.nombre(), e.celda);
                texto(r, 2, p.categoria(), e.celda);
                texto(r, 3, p.material(), e.celda);
                texto(r, 4, p.proveedor() == null ? "—" : p.proveedor(), e.celda);
                numero(r, 5, p.precio(), e.moneda);
                entero(r, 6, p.stockActual(), e.entero);
                entero(r, 7, p.stockMinimo(), e.entero);
                formula(r, 8, "F" + fila + "*G" + fila, e.moneda);
                texto(r, 9, p.estado(), switch (p.estado()) {
                    case "Agotado" -> e.agotado;
                    case "Stock bajo" -> e.bajo;
                    default -> e.disponible;
                });
            }
            if (!piezas.isEmpty()) {
                Row total = h1.createRow(fila);
                texto(total, 5, "Total", e.total);
                formula(total, 6, "SUM(G" + (primera + 1) + ":G" + fila + ")", e.totalEntero);
                formula(total, 8, "SUM(I" + (primera + 1) + ":I" + fila + ")", e.totalMoneda);
            }
            anchos(h1, 12, 32, 13, 17, 24, 13, 9, 9, 15, 13);
            h1.createFreezePane(0, primera);

            // ── Hoja 2: Resumen por tipo de joya ──
            Sheet h2 = wb.createSheet("Resumen por tipo de joya");
            fila = titulo(h2, e, "Resumen por tipo de joya", "Piezas activas", 5);
            fila = encabezado(h2, e, fila, "Tipo de joya", "Piezas", "Unidades", "Valor (S/)",
                    "Agotadas", "Stock bajo");
            int primeraRes = fila;
            for (CategoriaResumenDto c : resumen) {
                Row r = h2.createRow(fila++);
                texto(r, 0, c.categoria(), e.celda);
                entero(r, 1, c.productos(), e.entero);
                entero(r, 2, c.unidades(), e.entero);
                numero(r, 3, c.valor(), e.moneda);
                entero(r, 4, c.agotados(), e.entero);
                entero(r, 5, c.stockBajo(), e.entero);
            }
            Row tot = h2.createRow(fila);
            texto(tot, 0, "Total", e.total);
            for (int col = 1; col <= 5; col++) {
                String letra = String.valueOf((char) ('A' + col));
                formula(tot, col, "SUM(" + letra + (primeraRes + 1) + ":" + letra + fila + ")",
                        col == 3 ? e.totalMoneda : e.totalEntero);
            }
            anchos(h2, 22, 10, 11, 16, 11, 12);

            return escribir(wb, salida);
        }
    }

    private byte[] escribir(Workbook wb, ByteArrayOutputStream salida) throws IOException {
        wb.setForceFormulaRecalculation(true);
        wb.write(salida);
        return salida.toByteArray();
    }

    // ── Utilidades de hoja ─────────────────────────────────────────────

    private int titulo(Sheet h, Estilos e, String titulo, String subtitulo, int ultimaCol) {
        Row r0 = h.createRow(0);
        r0.setHeightInPoints(26);
        texto(r0, 0, titulo, e.titulo);
        h.addMergedRegion(new CellRangeAddress(0, 0, 0, ultimaCol));
        texto(h.createRow(1), 0, subtitulo, e.subtitulo);
        h.addMergedRegion(new CellRangeAddress(1, 1, 0, ultimaCol));
        return 3;
    }

    private int encabezado(Sheet h, Estilos e, int fila, String... columnas) {
        Row r = h.createRow(fila);
        r.setHeightInPoints(20);
        for (int i = 0; i < columnas.length; i++) texto(r, i, columnas[i], e.encabezado);
        return fila + 1;
    }

    private void anchos(Sheet h, int... caracteres) {
        for (int i = 0; i < caracteres.length; i++) h.setColumnWidth(i, caracteres[i] * 256);
    }

    private void texto(Row r, int col, String valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor == null ? "" : valor);
        c.setCellStyle(estilo);
    }

    private void numero(Row r, int col, BigDecimal valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor == null ? 0 : valor.doubleValue());
        c.setCellStyle(estilo);
    }

    private void entero(Row r, int col, int valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private void formula(Row r, int col, String formula, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellFormula(formula);
        c.setCellStyle(estilo);
    }

    /** Estilos del libro, con la paleta de la página (esmeralda y dorado). */
    private static final class Estilos {
        final CellStyle titulo, subtitulo, encabezado, celda, moneda, entero,
                total, totalMoneda, totalEntero, agotado, bajo, disponible;

        Estilos(Workbook wb) {
            DataFormat df = wb.createDataFormat();

            Font fTitulo = wb.createFont();
            fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 15);
            fTitulo.setColor(IndexedColors.DARK_GREEN.getIndex());
            titulo = wb.createCellStyle();
            titulo.setFont(fTitulo);
            titulo.setVerticalAlignment(VerticalAlignment.CENTER);

            Font fSub = wb.createFont();
            fSub.setItalic(true);
            fSub.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitulo = wb.createCellStyle();
            subtitulo.setFont(fSub);

            Font fEnc = wb.createFont();
            fEnc.setBold(true);
            fEnc.setColor(IndexedColors.WHITE.getIndex());
            encabezado = wb.createCellStyle();
            encabezado.setFont(fEnc);
            encabezado.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setVerticalAlignment(VerticalAlignment.CENTER);
            bordes(encabezado);

            celda = wb.createCellStyle();
            bordes(celda);

            moneda = wb.createCellStyle();
            moneda.cloneStyleFrom(celda);
            moneda.setDataFormat(df.getFormat("#,##0.00"));

            entero = wb.createCellStyle();
            entero.cloneStyleFrom(celda);
            entero.setDataFormat(df.getFormat("#,##0"));

            Font fTotal = wb.createFont();
            fTotal.setBold(true);
            total = wb.createCellStyle();
            total.cloneStyleFrom(celda);
            total.setFont(fTotal);
            total.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            total.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            totalMoneda = wb.createCellStyle();
            totalMoneda.cloneStyleFrom(total);
            totalMoneda.setDataFormat(df.getFormat("#,##0.00"));

            totalEntero = wb.createCellStyle();
            totalEntero.cloneStyleFrom(total);
            totalEntero.setDataFormat(df.getFormat("#,##0"));

            agotado = estado(wb, IndexedColors.DARK_RED);
            bajo = estado(wb, IndexedColors.BROWN);
            disponible = estado(wb, IndexedColors.GREEN);
        }

        private CellStyle estado(Workbook wb, IndexedColors color) {
            Font f = wb.createFont();
            f.setBold(true);
            f.setColor(color.getIndex());
            CellStyle s = wb.createCellStyle();
            bordes(s);
            s.setFont(f);
            return s;
        }

        private static void bordes(CellStyle s) {
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
            s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }
}
