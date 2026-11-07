package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoLoteDto {
    private Long id;
    private String fechaVencimiento;
    private Integer stockLoteActual;
    private BigDecimal precioOferta;
    private String nombreAlmacen;
}