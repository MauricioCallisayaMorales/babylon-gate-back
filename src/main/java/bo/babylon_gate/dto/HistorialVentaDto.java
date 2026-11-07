package bo.babylon_gate.dto;

import java.math.BigDecimal;

public class HistorialVentaDto {
    public Integer idDetalle; // ID de ventas_detalle (CRÍTICO para editar)
    public Integer idVenta;   // ID de ventas (para actualizar observación/fecha)
    public Integer idProducto; // ID de productos
    public Integer nro;
    public String fecha;
    public String codigo;
    public String producto;
    public String marca;
    public Integer cantidad;
    public BigDecimal precio;
    public BigDecimal precioTotal;
    public String obs;
}