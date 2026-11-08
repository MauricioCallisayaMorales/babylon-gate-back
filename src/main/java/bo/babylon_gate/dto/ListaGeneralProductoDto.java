package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListaGeneralProductoDto {
    private Long id;
    private String codigo;
    private String detalle;
    private String categoria;
    private String marca;
    private String unidad;
    private BigDecimal precioVenta;
    private BigDecimal precioMayorista;
    private Integer stockActual;
    private String idCodBarras;
    private String apiEstado;
    private String apiTransaccion;
    private String usuCre;
    private String fecCre;
    private String usuMod;
    private String fecMod;
    private String estado;
    private String imagenMiniatura;
}