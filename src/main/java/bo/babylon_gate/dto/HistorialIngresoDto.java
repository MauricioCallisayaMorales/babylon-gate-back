package bo.babylon_gate.dto;

import java.math.BigDecimal;

public class HistorialIngresoDto {
    private Long idCompra;
    private Integer nroProceso;
    private String codigo;
    private String fechaCompra;
    private BigDecimal costoTotalCompra;
    private String observacion;
    private String apiEstadoCompra;
    private String apiTransaccionCompra;

    private Long idCompraDetalle;
    private Long idProducto;
    private String nombreProducto;
    private Integer idPorveedor;
    private String proveedor;
    private String detalleProducto;
    private Integer idmarca;
    private String marca;
    private Integer cantidad;
    private BigDecimal costoUnit;
    private BigDecimal precioVenta;
    private String fechaVencimiento;
}