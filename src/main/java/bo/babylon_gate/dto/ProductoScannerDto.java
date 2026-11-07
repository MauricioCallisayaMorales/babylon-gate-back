package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoScannerDto {
    private boolean existeEnSistema;
    private Integer id;
    private String codigo;
    private String idCodBarras;
    private String detalle;
    private String categoria;
    private String marca;
    private String unidad;
    private String cantidad;
    private String imagenUrl;
    private BigDecimal precioVenta;
    private BigDecimal precioMayorista;
    private Integer stockActual;
}