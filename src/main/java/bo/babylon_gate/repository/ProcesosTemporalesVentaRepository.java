package bo.babylon_gate.repository;

import bo.babylon_gate.dto.TempVentaDetalleDto;
import bo.babylon_gate.dto.VentaDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProcesosTemporalesVentaRepository {

    @Inject
    EntityManager em;

    @Transactional
    public Long registrarNuevaVentaTemporal() {
        String sql = """
                    INSERT INTO public.temp_venta_proceso
                        (cliente_nombre, total_acumulado, api_estado, usu_cre, fec_cre)
                    VALUES
                        ('CLIENTE CASUAL', 0.00, 'ACTIVO', 'DBADMIN', now())
                    RETURNING nro_proceso;
                """;
        Number numeroProceso = (Number) em.createNativeQuery(sql).getSingleResult();
        return numeroProceso.longValue();
    }

    @Transactional
    public void registrarNuevoDetalleVentaTemporal(TempVentaDetalleDto dto) {
        String sql = """
                    INSERT INTO public.temp_venta_detalle
                        (id_temp_venta, id_producto, cantidad, precio_unitario, es_mayorista, precio_total, observacion, id_lote, fec_cre)
                    VALUES
                        (:idTempVenta, :idProducto, :cantidad, :precioUnitario, :esMayorista, :precioTotal, :observacion, :idLote, now());
                """;
        em.createNativeQuery(sql)
                .setParameter("idTempVenta", dto.getIdTempVenta())
                .setParameter("idProducto", dto.getIdProducto())
                .setParameter("cantidad", dto.getCantidad())
                .setParameter("precioUnitario", dto.getPrecioUnitario())
                .setParameter("esMayorista", dto.getEsMayorista() != null ? dto.getEsMayorista() : false)
                .setParameter("precioTotal", dto.getPrecioTotal())
                .setParameter("observacion", dto.getObservacion())
                .setParameter("idLote", dto.getIdLote())
                .executeUpdate();

        em.createNativeQuery("UPDATE public.productos SET stock_reservado = COALESCE(stock_reservado, 0) + :cant WHERE id = :idProd")
                .setParameter("cant", dto.getCantidad()).setParameter("idProd", dto.getIdProducto()).executeUpdate();

        if (dto.getIdLote() != null) {
            em.createNativeQuery("UPDATE public.producto_lotes SET stock_reservado_lote = COALESCE(stock_reservado_lote, 0) + :cant WHERE id = :idLote")
                    .setParameter("cant", dto.getCantidad()).setParameter("idLote", dto.getIdLote()).executeUpdate();
        }
    }

    public List<Long> obtenerListadoProcesosActivosVenta() {
        String sql = "SELECT nro_proceso FROM public.temp_venta_proceso WHERE api_estado = 'ACTIVO' ORDER BY nro_proceso ASC";
        List<Number> filas = em.createNativeQuery(sql).getResultList();
        List<Long> ids = new ArrayList<>();
        for (Number fila : filas) ids.add(fila.longValue());
        return ids;
    }

    public Optional<VentaDto> obtenerCabeceraProcesoVentaTemporal(Long nroProceso) {
        String sql = """
                    SELECT id, nro_proceso, total_acumulado, cliente_nombre, api_estado, usu_cre, fec_cre
                    FROM public.temp_venta_proceso WHERE nro_proceso = :nroProceso
                """;
        try {
            Object[] fila = (Object[]) em.createNativeQuery(sql)
                    .setParameter("nroProceso", nroProceso).getSingleResult();

            VentaDto dto = new VentaDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).longValue() : null);
            dto.setNroProceso(fila[1] != null ? fila[1].toString() : null);
            dto.setTotalVenta(fila[2] != null ? (java.math.BigDecimal) fila[2] : java.math.BigDecimal.ZERO);
            dto.setClienteNombre((String) fila[3]);
            dto.setApiEstado((String) fila[4]);
            dto.setUsuCre((String) fila[5]);
            dto.setFecCre(fila[6] != null ? fila[6].toString() : null);
            dto.setFechaVenta(String.valueOf(LocalTime.now()));
            dto.setApiTransaccion("CREAR");
            return Optional.of(dto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<TempVentaDetalleDto> obtenerListaDetallesVentaTemporal(Long idProcesoTemporal) {
        String sql = """
                    SELECT id, id_temp_venta, id_producto, cantidad, precio_unitario, es_mayorista, precio_total, observacion, id_lote, fec_cre
                    FROM public.temp_venta_detalle WHERE id_temp_venta = :idProceso
                    ORDER BY id ASC
                """;
        List<Object[]> filas = em.createNativeQuery(sql).setParameter("idProceso", idProcesoTemporal).getResultList();
        List<TempVentaDetalleDto> detalles = new ArrayList<>();
        for (Object[] fila : filas) {
            TempVentaDetalleDto dto = new TempVentaDetalleDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).longValue() : null);
            dto.setIdTempVenta(fila[1] != null ? ((Number) fila[1]).longValue() : null);
            dto.setIdProducto(fila[2] != null ? ((Number) fila[2]).longValue() : null);
            dto.setCantidad(fila[3] != null ? ((Number) fila[3]).intValue() : 0);
            dto.setPrecioUnitario(fila[4] != null ? (java.math.BigDecimal) fila[4] : java.math.BigDecimal.ZERO);
            dto.setEsMayorista((Boolean) fila[5]);
            dto.setPrecioTotal(fila[6] != null ? (java.math.BigDecimal) fila[6] : java.math.BigDecimal.ZERO);
            dto.setObservacion((String) fila[7]);
            dto.setIdLote(fila[8] != null ? ((Number) fila[8]).longValue() : null);
            dto.setFecCre(fila[9] != null ? fila[9].toString() : null);
            detalles.add(dto);
        }
        return detalles;
    }

    @Transactional
    public void cancelarProcesoVenta(Long nroProceso) {
        // Buscar todos los detalles para liberar reservas
        List<Object[]> detalles = em.createNativeQuery("SELECT id_producto, cantidad, id_lote FROM public.temp_venta_detalle WHERE id_temp_venta = (SELECT id FROM public.temp_venta_proceso WHERE nro_proceso = :nroProceso)")
                .setParameter("nroProceso", nroProceso).getResultList();

        for(Object[] det : detalles) {
            em.createNativeQuery("UPDATE public.productos SET stock_reservado = stock_reservado - :cant WHERE id = :idProd")
                    .setParameter("cant", ((Number)det[1]).intValue()).setParameter("idProd", ((Number)det[0]).longValue()).executeUpdate();
            if (det[2] != null) {
                em.createNativeQuery("UPDATE public.producto_lotes SET stock_reservado_lote = stock_reservado_lote - :cant WHERE id = :idLote")
                        .setParameter("cant", ((Number)det[1]).intValue()).setParameter("idLote", ((Number)det[2]).longValue()).executeUpdate();
            }
        }
        em.createNativeQuery("DELETE FROM public.temp_venta_proceso WHERE nro_proceso = :nroProceso").setParameter("nroProceso", nroProceso).executeUpdate();
    }

    @Transactional
    public void eliminarDetalleVentaTemporal(Long idDetalle) {
        Object[] datos = (Object[]) em.createNativeQuery("SELECT id_producto, cantidad, id_lote FROM public.temp_venta_detalle WHERE id = :id")
                .setParameter("id", idDetalle).getSingleResult();

        em.createNativeQuery("DELETE FROM public.temp_venta_detalle WHERE id = :id")
                .setParameter("id", idDetalle).executeUpdate();

        em.createNativeQuery("UPDATE public.productos SET stock_reservado = stock_reservado - :cant WHERE id = :idProd")
                .setParameter("cant", ((Number)datos[1]).intValue()).setParameter("idProd", ((Number)datos[0]).longValue()).executeUpdate();

        if (datos[2] != null) {
            em.createNativeQuery("UPDATE public.producto_lotes SET stock_reservado_lote = stock_reservado_lote - :cant WHERE id = :idLote")
                    .setParameter("cant", ((Number)datos[1]).intValue()).setParameter("idLote", ((Number)datos[2]).longValue()).executeUpdate();
        }
    }

    @Transactional
    public void actualizarPrecioDetalleVentaTemporal(Long idDetalle, java.math.BigDecimal nuevoTotal, java.math.BigDecimal nuevoPrecioUnitario) {
        em.createNativeQuery("UPDATE public.temp_venta_detalle SET precio_total = :total, precio_unitario = :unitario WHERE id = :id")
                .setParameter("total", nuevoTotal)
                .setParameter("unitario", nuevoPrecioUnitario)
                .setParameter("id", idDetalle)
                .executeUpdate();
    }
}