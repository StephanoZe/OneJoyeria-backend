package pe.onejoyeria.controlador;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.onejoyeria.servicio.ExcelServicio;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Exportación a Excel. El PDF se genera desde la página (vista de impresión). */
@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExcelServicio excel;

    public ReporteControlador(ExcelServicio excel) {
        this.excel = excel;
    }

    // GET /api/reportes/movimientos/excel?desde=&hasta=&categoriaId=
    @GetMapping("/movimientos/excel")
    public ResponseEntity<byte[]> movimientosExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId) throws IOException {
        return adjunto(excel.movimientos(desde, hasta, categoriaId), "OneJoyeria_Movimientos");
    }

    // GET /api/reportes/inventario/excel?categoriaId=
    @GetMapping("/inventario/excel")
    public ResponseEntity<byte[]> inventarioExcel(@RequestParam(required = false) Long categoriaId) throws IOException {
        return adjunto(excel.inventarioValorizado(categoriaId), "OneJoyeria_Inventario");
    }

    private ResponseEntity<byte[]> adjunto(byte[] archivo, String prefijo) {
        String nombre = prefijo + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .body(archivo);
    }
}
