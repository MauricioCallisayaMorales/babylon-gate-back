package bo.babylon_gate.repository;

import bo.babylon_gate.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CompraRepository {
    @Inject
    EntityManager em;

    @Transactional
    public void registrarCompraCompleta(CompraDto cabecera, List<TempIngresoDetalleDto> detalles, java.math.BigDecimal totalManual) {
        String observacionGeneral = cabecera.getObservacion();
        java.math.BigDecimal sumaCalculada = java.math.BigDecimal.ZERO;

        for (TempIngresoDetalleDto det : detalles) {
            if (det.getObservacion() != null && !det.getObservacion().trim().isEmpty()) {
                observacionGeneral = det.getObservacion();
            }
            sumaCalculada = sumaCalculada.add(det.getCostoTotal() != null ? det.getCostoTotal() : java.math.BigDecimal.ZERO);
        }

        java.math.BigDecimal totalDefinitivo = totalManual != null ? totalManual : sumaCalculada;

        String sqlCabecera = """
                INSERT INTO public.compras
                    (nro_proceso, fecha_compra, total_compra, observacion, api_estado, api_transaccion, usu_cre, fec_cre)
                VALUES
                    (:nroProceso, now(), :totalDefinitivo, :observacion, 'CREADO', 'CREAR', 'DBADMIN', now())
                RETURNING id;
                """;
        Number idCompraOficial = (Number) em.createNativeQuery(sqlCabecera)
                .setParameter("nroProceso", cabecera.getNroProceso())
                .setParameter("totalDefinitivo", totalDefinitivo)
                .setParameter("observacion", observacionGeneral)
                .getSingleResult();

        long idCompra = idCompraOficial.longValue();

        String sqlDetalle = """
                    INSERT INTO public.compras_detalle (
                        id_compra, id_producto, id_proveedor, cantidad, costo_unidad, 
                        fecha_vencimiento, costo_total, stock_lote, api_estado, api_transaccion, usu_cre, fec_cre
                    ) VALUES (
                        :idCompra, :idProducto, :idProveedor, :cantidad, :costoUnidad, 
                        CAST(:fechaVencimiento AS DATE), :costoTotal, :stockLote, 'CREADO', 'CREAR', 'DBADMIN', now()
                    ) RETURNING id;
                """;

        String sqlActualizarStock = """
                    UPDATE public.productos 
                    SET 
                        stock_actual = stock_actual + :cantidad,
                        usu_mod = 'DBADMIN',
                        fec_mod = now()
                    WHERE id = :idProducto;
                """;

        String sqlKardex = """
                INSERT INTO public.kardex_inventario (
                    id_producto, tipo_movimiento, cantidad_base_movida, saldo_stock_base, referencia_proceso, usu_cre
                ) VALUES (
                    :idProducto, 'INGRESO_COMPRA', :cantidad,
                    (SELECT COALESCE(stock_actual, 0) FROM public.productos WHERE id = :idProducto),
                    :referencia, 'DBADMIN'
                );
                """;

        String sqlLote = """
                    INSERT INTO public.producto_lotes (
                        id_producto, id_compra_detalle, fecha_vencimiento, stock_lote_actual, usu_cre
                    ) VALUES (
                        :idProducto, :idCompraDetalle, CAST(:fechaVencimiento AS DATE), :cantidad, 'DBADMIN'
                    );
                """;

        for (TempIngresoDetalleDto detTemp : detalles) {
            Number idDetalleNuevo = (Number) em.createNativeQuery(sqlDetalle)
                    .setParameter("idCompra", idCompra)
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .setParameter("idProveedor", detTemp.getIdProveedor())
                    .setParameter("cantidad", detTemp.getCantidad())
                    .setParameter("costoUnidad", detTemp.getCostoUnitario())
                    .setParameter("fechaVencimiento", detTemp.getFechaVencimiento())
                    .setParameter("costoTotal", detTemp.getCostoTotal())
                    .setParameter("stockLote", detTemp.getCantidad())
                    .getSingleResult();

            em.createNativeQuery(sqlActualizarStock)
                    .setParameter("cantidad", detTemp.getCantidad())
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .executeUpdate();

            em.createNativeQuery(sqlKardex)
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .setParameter("cantidad", detTemp.getCantidad())
                    .setParameter("referencia", cabecera.getNroProceso())
                    .executeUpdate();

            em.createNativeQuery(sqlLote)
                    .setParameter("idProducto", detTemp.getIdProducto())
                    .setParameter("idCompraDetalle", idDetalleNuevo.longValue())
                    .setParameter("fechaVencimiento", detTemp.getFechaVencimiento())
                    .setParameter("cantidad", detTemp.getCantidad())
                    .executeUpdate();
        }

        String sqlEliminarTemporal = "DELETE FROM public.temp_ingreso_proceso WHERE id = :idTempProceso;";
        em.createNativeQuery(sqlEliminarTemporal)
                .setParameter("idTempProceso", cabecera.getId())
                .executeUpdate();
    }

    public List<CompraHistorialGeneralDto> listarComprasGeneral() {
        String sql = """
                    SELECT id, nro_proceso, fecha_compra, total_compra, observacion, api_estado 
                    FROM public.compras 
                    ORDER BY id DESC;
                """;
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        List<CompraHistorialGeneralDto> resultado = new ArrayList<>();

        for (Object[] fila : filas) {
            CompraHistorialGeneralDto dto = new CompraHistorialGeneralDto();
            dto.setId(((Number) fila[0]).longValue());
            dto.setNroProceso((String) fila[1]);
            dto.setFechaCompra(fila[2] != null ? fila[2].toString() : null);
            dto.setTotalCompra((java.math.BigDecimal) fila[3]);
            dto.setObservacion((String) fila[4]);
            dto.setApiEstado((String) fila[5]);
            resultado.add(dto);
        }
        return resultado;
    }

    public Optional<CompraCompletaDetalladaDto> buscarCompraCabeceraPorId(Long idCompra) {
        String sql = """
                    SELECT id, nro_proceso, fecha_compra, total_compra, observacion, api_estado 
                    FROM public.compras 
                    WHERE id = :idCompra;
                """;
        try {
            Object[] fila = (Object[]) em.createNativeQuery(sql)
                    .setParameter("idCompra", idCompra)
                    .getSingleResult();

            CompraCompletaDetalladaDto dto = new CompraCompletaDetalladaDto();
            dto.setId(((Number) fila[0]).longValue());
            dto.setNroProceso((String) fila[1]);
            dto.setFechaCompra(fila[2] != null ? fila[2].toString() : null);
            dto.setTotalCompra((java.math.BigDecimal) fila[3]);
            dto.setObservacion((String) fila[4]);
            dto.setApiEstado((String) fila[5]);
            return Optional.of(dto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<ItemDetalleCompraDto> listarDetallesPorCompraId(Long idCompra) {
        String sql = """
                    SELECT 
                        cd.id AS id_detalle,
                        cd.id_producto,
                        p.codigo AS codigo_producto,
                        p.detalle AS nombre_producto,
                        m.nombre AS marca_producto,
                        cd.id_proveedor,
                        prov.nombre AS nombre_proveedor,
                        cd.cantidad,
                        cd.costo_unidad,
                        cd.costo_total,
                        cd.fecha_vencimiento
                    FROM public.compras_detalle cd
                    INNER JOIN public.productos p ON cd.id_producto = p.id
                    LEFT JOIN public.cat_marcas m ON p.id_marca = m.id
                    LEFT JOIN public.cat_proveedores prov ON cd.id_proveedor = prov.id
                    WHERE cd.id_compra = :idCompra
                    ORDER BY cd.id ASC;
                """;
        List<Object[]> filas = em.createNativeQuery(sql)
                .setParameter("idCompra", idCompra)
                .getResultList();

        List<ItemDetalleCompraDto> detalles = new ArrayList<>();
        for (Object[] fila : filas) {
            ItemDetalleCompraDto dto = new ItemDetalleCompraDto();
            dto.setIdDetalle(((Number) fila[0]).longValue());
            dto.setIdProducto(((Number) fila[1]).longValue());
            dto.setCodigoProducto((String) fila[2]);
            dto.setNombreProducto((String) fila[3]);
            dto.setMarcaProducto((String) fila[4]);
            dto.setIdProveedor(fila[5] != null ? ((Number) fila[5]).intValue() : null);
            dto.setNombreProveedor((String) fila[6]);
            dto.setCantidad(((Number) fila[7]).intValue());
            dto.setCostoUnidad((java.math.BigDecimal) fila[8]);
            dto.setCostoTotal((java.math.BigDecimal) fila[9]);
            dto.setFechaVencimiento(fila[10] != null ? fila[10].toString() : null);
            detalles.add(dto);
        }
        return detalles;
    }

    @Transactional
    public void actualizarCompraYStock(CompraCompletaDetalladaDto dto) {

        String sqlUpdateCabecera = """
                    UPDATE public.compras 
                    SET total_compra = :total, observacion = :obs, api_estado = :estado, usu_mod = 'DBADMIN', fec_mod = now()
                    WHERE id = :id;
                """;
        em.createNativeQuery(sqlUpdateCabecera)
                .setParameter("total", dto.getTotalCompra())
                .setParameter("obs", dto.getObservacion())
                .setParameter("estado", dto.getApiEstado())
                .setParameter("id", dto.getId())
                .executeUpdate();

        String sqlGetCantidadOriginal = "SELECT cantidad FROM public.compras_detalle WHERE id = :idDetalle;";

        String sqlUpdateDetalle = """
                    UPDATE public.compras_detalle 
                    SET id_proveedor = :idProv, cantidad = :cant, costo_unidad = :costoUnit, 
                        fecha_vencimiento = CAST(:fecVen AS DATE), costo_total = :costoTot, stock_lote = :cant,
                        usu_mod = 'DBADMIN', fec_mod = now()
                    WHERE id = :idDetalle;
                """;

        String sqlModificarStockProducto = """
                    UPDATE public.productos 
                    SET stock_actual = COALESCE(stock_actual, 0) + :diferencia, usu_mod = 'DBADMIN', fec_mod = now()
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

        for (ItemDetalleCompraDto item : dto.getDetalles()) {

            int cantidadOriginal = 0;
            try {
                Number cantBase = (Number) em.createNativeQuery(sqlGetCantidadOriginal)
                        .setParameter("idDetalle", item.getIdDetalle())
                        .getSingleResult();
                cantidadOriginal = cantBase.intValue();
            } catch (NoResultException e) {
                throw new IllegalArgumentException("No se encontró el detalle de compra ID: " + item.getIdDetalle());
            }

            int diferenciaDeStock = item.getCantidad() - cantidadOriginal;

            em.createNativeQuery(sqlUpdateDetalle)
                    .setParameter("idProv", item.getIdProveedor())
                    .setParameter("cant", item.getCantidad())
                    .setParameter("costoUnit", item.getCostoUnidad())
                    .setParameter("fecVen", item.getFechaVencimiento())
                    .setParameter("costoTot", item.getCostoTotal())
                    .setParameter("idDetalle", item.getIdDetalle())
                    .executeUpdate();

            if (diferenciaDeStock != 0) {
                em.createNativeQuery(sqlModificarStockProducto)
                        .setParameter("diferencia", diferenciaDeStock)
                        .setParameter("idProducto", item.getIdProducto())
                        .executeUpdate();

                em.createNativeQuery(sqlKardexAjuste)
                        .setParameter("idProducto", item.getIdProducto())
                        .setParameter("diferencia", diferenciaDeStock)
                        .setParameter("referencia", "MOD-" + dto.getNroProceso())
                        .executeUpdate();
            }
        }
    }

    @Transactional
    public void anularCompraYRevertirStock(Long idCompra) {
        // Validar que no se haya consumido stock de los lotes de esta compra
        String sqlVal = "SELECT COUNT(*) FROM public.compras_detalle cd JOIN public.producto_lotes pl ON cd.id = pl.id_compra_detalle WHERE cd.id_compra = :idCompra AND pl.stock_lote_actual < cd.cantidad";
        Number consumidos = (Number) em.createNativeQuery(sqlVal).setParameter("idCompra", idCompra).getSingleResult();
        if (consumidos.intValue() > 0) {
            throw new IllegalStateException("No se puede anular la compra: Ya se han vendido productos pertenecientes a este ingreso.");
        }

        em.createNativeQuery("UPDATE public.compras SET api_estado = 'ANULADO' WHERE id = :idCompra")
                .setParameter("idCompra", idCompra).executeUpdate();

        em.createNativeQuery("UPDATE public.producto_lotes SET api_estado = 'ANULADO', stock_lote_actual = 0 WHERE id_compra_detalle IN (SELECT id FROM public.compras_detalle WHERE id_compra = :idCompra)")
                .setParameter("idCompra", idCompra).executeUpdate();

        String sqlProds = "SELECT id_producto, cantidad, (SELECT nro_proceso FROM public.compras WHERE id = :idCompra) FROM public.compras_detalle WHERE id_compra = :idCompra";
        List<Object[]> prods = em.createNativeQuery(sqlProds).setParameter("idCompra", idCompra).getResultList();

        for (Object[] p : prods) {
            em.createNativeQuery("UPDATE public.productos SET stock_actual = stock_actual - :cant WHERE id = :idProd")
                    .setParameter("cant", ((Number) p[1]).intValue())
                    .setParameter("idProd", ((Number) p[0]).longValue())
                    .executeUpdate();

            em.createNativeQuery("INSERT INTO public.kardex_inventario (id_producto, tipo_movimiento, cantidad_base_movida, saldo_stock_base, referencia_proceso, usu_cre) VALUES (:idProd, 'ANULACION_COMPRA', :cantNegativa, (SELECT stock_actual FROM public.productos WHERE id = :idProd), :ref, 'DBADMIN')")
                    .setParameter("idProd", ((Number) p[0]).longValue())
                    .setParameter("cantNegativa", -((Number) p[1]).intValue())
                    .setParameter("ref", "ANUL-" + (String) p[2])
                    .executeUpdate();
        }
    }
}