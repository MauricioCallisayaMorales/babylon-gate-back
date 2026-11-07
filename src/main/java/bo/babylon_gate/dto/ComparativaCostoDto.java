package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComparativaCostoDto {
    private Long idProducto;
    private String nombreProducto;
    private Integer idProveedor;
    private String nombreProveedor;
    private Integer idUnidad;
    private String nombreUnidad;
    private BigDecimal costo;
}