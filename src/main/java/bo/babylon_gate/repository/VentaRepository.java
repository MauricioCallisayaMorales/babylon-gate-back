package bo.babylon_gate.repository;

import bo.babylon_gate.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VentaRepository {

    @Inject
    EntityManager em;

    @Transactional
    public void registrarVentaCompleta(VentaDto cabecera, List<TempVentaDetalleDto> detalles, BigDecimal totalManual, String metodoPago, java.math.BigDecimal montoRecibido, java.math.BigDecimal cambioEntregado) {
        String observacionGeneral = cabecera.getObservacion();
        BigDecimal sumaCalculada = BigDecimal.ZERO;

        for (TempVentaDetalleDto det : detalles) {
            if (det.getObservacion() != null && !det.getObservacion().trim().isEmpty()) {
                observacionGeneral = det.getObservacion();
            }
            sumaCalculada = sumaCalculada.add(det.getPrecioTotal() != null ? det.getPrecioTotal() : BigDecimal.ZERO);
        }

        BigDecimal totalDefinitivo = totalManual != null ? totalManual : sumaCalculada;

        String sqlCabecera = """
                INSERT INTO public.ventas
                    (nro_proceso, fecha_venta, total_venta, cliente_nombre, observacion, api_estado, api_transaccion, usu_cre, fec_cre, metodo_pago, monto_recibido, cambio_entregado)
                VALUES
                    (:nroProceso, now(), :totalDefinitivo, :cliente, :observacion, 'FINALIZADO', 'CREAR', 'DBADMIN', now(), :metodoPago, :montoRecibido, :cambioEntregado)
                RETURNING id;
                """;

        Number idVentaOficial = (Number) em.createNativeQuery(sqlCabecera)
                .setParameter("nroProceso", cabecera.getNroProceso())
                .setParameter("totalDefinitivo", totalDefinitivo)
                .setParameter("cliente", cabecera.getClienteNombre())
                .setParameter("observacion", observacionGeneral)
                .setParameter("metodoPago", metodoPago != null ? metodoPago : "EFECTIVO")
                .setParameter("montoRecibido", montoRecibido != null ? montoRecibido : totalDefinitivo)
                .setParameter("cambioEntregado", cambioEntregado != null ? cambioEntregado : java.math.BigDecimal.ZERO)
                .getSingleResult();

        long idVenta = idVentaOficial.longValue();

        String sqlDetalle = """
                    INSERT INTO public.ventas_detalle (
                        id_venta, id_producto, cantidad, precio_unitario, precio_total, es_mayorista, observacion, 
                        api_estado, api_transaccion, usu_cre, fec_cre
                    ) VALUES (
                        :idVenta, :idProducto, :cantidad, :precioUnitario, :precioTotal, :esMayorista, :observacion,
                        'CREADO', 'CREAR', 'DBADMIN', now()
                    ) RETURNING id;
                """;

        String sqlActualizarStock = "UPDATE public.productos SET stock_actual = stock_actual - :cantidad WHERE id = :idProducto;";

        String sqlKardex = """
                    INSERT INTO public.kardex_inventario (id_producto, tipo_movimiento, cantidad_base_movida, saldo_stock_base, referencia_proceso, usu_cre) 
                    VALUES (:idProducto, 'SALIDA_VENTA', :cantidadNegativa, (SELECT stock_actual FROM public.productos WHERE id = :idProducto), :referencia, 'DBADMIN');
                """;

        String sqlBuscarLotes = "SELECT id, stock_lote_actual FROM public.producto_lotes WHERE id_producto = :idProducto AND api_estado = 'ACTIVO' ORDER BY fecha_vencimiento ASC NULLS LAST;";
        String sqlDescontarLote = "UPDATE public.producto_lotes SET stock_lote_actual = :nuevoStock, api_estado = :estado WHERE id = :idLote;";
        String sqlRelacionVentaLote = "INSERT INTO public.ventas_detalle_lotes (id_venta_detalle, id_lote, cantidad) VALUES (:idDetalle, :idLote, :cantTomada);";
        String sqlLoteEspecifico = "SELECT id, stock_lote_actual FROM public.producto_lotes WHERE id = :idLote AND api_estado = 'ACTIVO';";

        for (TempVentaDetalleDto detTemp : detalles) {
            Number idDetalleGenerado = (Number) em.createNativeQuery(sqlDetalle)
                    .setParameter("idVenta", idVenta)
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .setParameter("cantidad", detTemp.getCantidad())
                    .setParameter("precioUnitario", detTemp.getPrecioUnitario())
                    .setParameter("precioTotal", detTemp.getPrecioTotal())
                    .setParameter("esMayorista", detTemp.getEsMayorista() != null ? detTemp.getEsMayorista() : false)
                    .setParameter("observacion", detTemp.getObservacion())
                    .getSingleResult();

            long idDetalle = idDetalleGenerado.longValue();

            em.createNativeQuery(sqlActualizarStock)
                    .setParameter("cantidad", detTemp.getCantidad())
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .executeUpdate();

            em.createNativeQuery(sqlKardex)
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .setParameter("cantidadNegativa", -detTemp.getCantidad())
                    .setParameter("referencia", cabecera.getNroProceso())
                    .executeUpdate();

            int cantidadADescontar = detTemp.getCantidad();
            List<Object[]> lotes;

            if (detTemp.getIdLote() != null) {
                lotes = em.createNativeQuery(sqlLoteEspecifico).setParameter("idLote", detTemp.getIdLote()).getResultList();
            } else {
                lotes = em.createNativeQuery(sqlBuscarLotes).setParameter("idProducto", detTemp.getIdProducto()).getResultList();
            }

            for (Object[] lote : lotes) {
                if (cantidadADescontar <= 0) break;

                long idLote = ((Number) lote[0]).longValue();
                int stockLote = ((Number) lote[1]).intValue();
                int cantTomada = 0;
                int nuevoStock;
                String estadoLote = "ACTIVO";

                if (stockLote <= cantidadADescontar) {
                    cantTomada = stockLote;
                    cantidadADescontar -= stockLote;
                    nuevoStock = 0;
                    estadoLote = "AGOTADO";
                } else {
                    cantTomada = cantidadADescontar;
                    nuevoStock = stockLote - cantidadADescontar;
                    cantidadADescontar = 0;
                }

                em.createNativeQuery(sqlDescontarLote)
                        .setParameter("nuevoStock", nuevoStock)
                        .setParameter("estado", estadoLote)
                        .setParameter("idLote", idLote)
                        .executeUpdate();

                em.createNativeQuery(sqlRelacionVentaLote)
                        .setParameter("idDetalle", idDetalle)
                        .setParameter("idLote", idLote)
                        .setParameter("cantTomada", cantTomada)
                        .executeUpdate();
            }
        }
        em.createNativeQuery("DELETE FROM public.temp_venta_proceso WHERE id = :idTempProceso;").setParameter("idTempProceso", cabecera.getId()).executeUpdate();
    }

    public List<VentaHistorialGeneralDto> listarVentasGeneral() {
        String sql = "SELECT id, nro_proceso, fecha_venta, total_venta, cliente_nombre, api_estado FROM public.ventas ORDER BY id DESC;";
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        List<VentaHistorialGeneralDto> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            VentaHistorialGeneralDto dto = new VentaHistorialGeneralDto();
            dto.setId(((Number) fila[0]).longValue());
            dto.setNroProceso((String) fila[1]);
            dto.setFechaVenta(fila[2] != null ? fila[2].toString() : null);
            dto.setTotalVenta((java.math.BigDecimal) fila[3]);
            dto.setClienteNombre((String) fila[4]);
            dto.setApiEstado((String) fila[5]);
            resultado.add(dto);
        }
        return resultado;
    }

    public Optional<VentaCompletaDetalladaDto> buscarVentaCabeceraPorId(Long idVenta) {
        String sql = "SELECT id, nro_proceso, fecha_venta, total_venta, cliente_nombre, observacion, api_estado FROM public.ventas WHERE id = :idVenta;";
        try {
            Object[] fila = (Object[]) em.createNativeQuery(sql).setParameter("idVenta", idVenta).getSingleResult();
            VentaCompletaDetalladaDto dto = new VentaCompletaDetalladaDto();
            dto.setId(((Number) fila[0]).longValue());
            dto.setNroProceso((String) fila[1]);
            dto.setFechaVenta(fila[2] != null ? fila[2].toString() : null);
            dto.setTotalVenta((java.math.BigDecimal) fila[3]);
            dto.setClienteNombre((String) fila[4]);
            dto.setObservacion((String) fila[5]);
            dto.setApiEstado((String) fila[6]);
            return Optional.of(dto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<ItemDetalleVentaDto> listarDetallesPorVentaId(Long idVenta) {
        String sql = """
                    SELECT vd.id AS id_detalle, vd.id_producto, p.codigo AS codigo_producto, p.detalle AS nombre_producto, 
                           m.nombre AS marca_producto, vd.cantidad, vd.precio_unitario, vd.precio_total, 
                           vd.es_mayorista, vd.observacion,
                           STRING_AGG(COALESCE(TO_CHAR(pl.fecha_vencimiento, 'DD/MM/YYYY'), 'Sin Vencimiento'), ' | ') AS lotes
                    FROM public.ventas_detalle vd
                    INNER JOIN public.productos p ON vd.id_producto = p.id
                    LEFT JOIN public.cat_marcas m ON p.id_marca = m.id
                    LEFT JOIN public.ventas_detalle_lotes vdl ON vd.id = vdl.id_venta_detalle
                    LEFT JOIN public.producto_lotes pl ON vdl.id_lote = pl.id
                    WHERE vd.id_venta = :idVenta 
                    GROUP BY vd.id, vd.id_producto, p.codigo, p.detalle, m.nombre, vd.cantidad, vd.precio_unitario, vd.precio_total, vd.es_mayorista, vd.observacion
                    ORDER BY vd.id ASC;
                """;
        List<Object[]> filas = em.createNativeQuery(sql).setParameter("idVenta", idVenta).getResultList();
        List<ItemDetalleVentaDto> detalles = new ArrayList<>();
        for (Object[] fila : filas) {
            ItemDetalleVentaDto dto = new ItemDetalleVentaDto();
            dto.setIdDetalle(((Number) fila[0]).longValue());
            dto.setIdProducto(((Number) fila[1]).longValue());
            dto.setCodigoProducto((String) fila[2]);
            dto.setNombreProducto((String) fila[3]);
            dto.setMarcaProducto((String) fila[4]);
            dto.setCantidad(((Number) fila[5]).intValue());
            dto.setPrecioUnitario((java.math.BigDecimal) fila[6]);
            dto.setPrecioTotal((java.math.BigDecimal) fila[7]);
            dto.setEsMayorista((Boolean) fila[8]);
            dto.setObservacion((String) fila[9]);
            dto.setLotesAsignados((String) fila[10]);
            detalles.add(dto);
        }
        return detalles;
    }

    @Transactional
    public void actualizarVentaYStock(VentaCompletaDetalladaDto dto) {
        String sqlUpdateCabecera = """
                    UPDATE public.ventas 
                    SET total_venta = :total, cliente_nombre = :cliente, observacion = :obs, api_estado = :estado, usu_mod = 'DBADMIN', fec_mod = now()
                    WHERE id = :id;
                """;
        em.createNativeQuery(sqlUpdateCabecera)
                .setParameter("total", dto.getTotalVenta())
                .setParameter("cliente", dto.getClienteNombre())
                .setParameter("obs", dto.getObservacion())
                .setParameter("estado", dto.getApiEstado())
                .setParameter("id", dto.getId())
                .executeUpdate();

        String sqlGetCantidadOriginal = "SELECT cantidad FROM public.ventas_detalle WHERE id = :idDetalle;";

        String sqlUpdateDetalle = """
                    UPDATE public.ventas_detalle 
                    SET cantidad = :cant, precio_unitario = :precio, precio_total = :total, 
                        es_mayorista = :mayorista, observacion = :obs, usu_mod = 'DBADMIN', fec_mod = now()
                    WHERE id = :idDetalle;
                """;

        String sqlModificarStockProducto = """
                    UPDATE public.productos 
                    SET stock_actual = stock_actual - :diferencia, usu_mod = 'DBADMIN', fec_mod = now()
                    WHERE id = :idProducto;
                """;

        String sqlKardexAjuste = """
                    INSERT INTO public.kardex_inventario (
                        id_producto, tipo_movimiento, cantidad_base_movida, saldo_stock_base, referencia_proceso, usu_cre
                    ) VALUES (
                        :idProducto, 'AJUSTE', :diferencia, 
                        (SELECT stock_actual FROM public.productos WHERE id = :idProducto), 
                        :referencia, 'DBADMIN'
                    );
                """;

        for (ItemDetalleVentaDto item : dto.getDetalles()) {
            int cantidadOriginal = 0;
            try {
                Number cantBase = (Number) em.createNativeQuery(sqlGetCantidadOriginal)
                        .setParameter("idDetalle", item.getIdDetalle()).getSingleResult();
                cantidadOriginal = cantBase.intValue();
            } catch (NoResultException e) {
                throw new IllegalArgumentException("No se encontró el detalle de venta ID: " + item.getIdDetalle());
            }

            int diferenciaDeStock = item.getCantidad() - cantidadOriginal;

            em.createNativeQuery(sqlUpdateDetalle)
                    .setParameter("cant", item.getCantidad())
                    .setParameter("precio", item.getPrecioUnitario())
                    .setParameter("total", item.getPrecioTotal())
                    .setParameter("mayorista", item.getEsMayorista() != null ? item.getEsMayorista() : false)
                    .setParameter("obs", item.getObservacion())
                    .setParameter("idDetalle", item.getIdDetalle())
                    .executeUpdate();

            if (diferenciaDeStock != 0) {
                em.createNativeQuery(sqlModificarStockProducto)
                        .setParameter("diferencia", diferenciaDeStock)
                        .setParameter("idProducto", item.getIdProducto())
                        .executeUpdate();

                em.createNativeQuery(sqlKardexAjuste)
                        .setParameter("idProducto", item.getIdProducto())
                        .setParameter("diferencia", -diferenciaDeStock)
                        .setParameter("referencia", "MOD-" + dto.getNroProceso())
                        .executeUpdate();
            }
        }
    }

    @Transactional
    public void anularVentaYRevertirStock(Long idVenta) {
        em.createNativeQuery("UPDATE public.ventas SET api_estado = 'ANULADO' WHERE id = :idVenta")
                .setParameter("idVenta", idVenta).executeUpdate();

        String sqlLotes = "SELECT id_lote, cantidad FROM public.ventas_detalle_lotes vdl JOIN public.ventas_detalle vd ON vdl.id_venta_detalle = vd.id WHERE vd.id_venta = :idVenta";
        List<Object[]> lotes = em.createNativeQuery(sqlLotes).setParameter("idVenta", idVenta).getResultList();

        for (Object[] lote : lotes) {
            em.createNativeQuery("UPDATE public.producto_lotes SET stock_lote_actual = stock_lote_actual + :cant, api_estado = 'ACTIVO' WHERE id = :idLote")
                    .setParameter("cant", ((Number) lote[1]).intValue())
                    .setParameter("idLote", ((Number) lote[0]).longValue())
                    .executeUpdate();
        }

        String sqlProds = "SELECT id_producto, cantidad, (SELECT nro_proceso FROM public.ventas WHERE id = :idVenta) FROM public.ventas_detalle WHERE id_venta = :idVenta";
        List<Object[]> prods = em.createNativeQuery(sqlProds).setParameter("idVenta", idVenta).getResultList();

        for (Object[] p : prods) {
            em.createNativeQuery("UPDATE public.productos SET stock_actual = stock_actual + :cant WHERE id = :idProd")
                    .setParameter("cant", ((Number) p[1]).intValue())
                    .setParameter("idProd", ((Number) p[0]).longValue())
                    .executeUpdate();

            em.createNativeQuery("INSERT INTO public.kardex_inventario (id_producto, tipo_movimiento, cantidad_base_movida, saldo_stock_base, referencia_proceso, usu_cre) VALUES (:idProd, 'ANULACION_VENTA', :cant, (SELECT stock_actual FROM public.productos WHERE id = :idProd), :ref, 'DBADMIN')")
                    .setParameter("idProd", ((Number) p[0]).longValue())
                    .setParameter("cant", ((Number) p[1]).intValue())
                    .setParameter("ref", "ANUL-" + (String) p[2])
                    .executeUpdate();
        }
    }
}