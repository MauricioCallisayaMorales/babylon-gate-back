package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoInventarioDto {
    private Long id;
    private String codigo;
    private String detalle;
    private String categoria;
    private String marca;
    private String unidad;
    private BigDecimal precioVenta;
    private BigDecimal precioMayorista;
    private Integer cantidad;
    private String fechaIngreso;
    private String fechaVencimiento;

    private Integer vidaUtilDias;
    private Integer alertaAmarillaPorc;
    private Integer alertaRojaPorc;
    private String imagenMiniatura;
}