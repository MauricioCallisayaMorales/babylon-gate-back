package bo.babylon_gate.repository;

import bo.babylon_gate.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalTime.now;

@ApplicationScoped
public class ProcesosTemporalesRepository {

    @Inject
    EntityManager em;

    @Transactional
    public Long registrarNuevoIngresoTemporal() {
        String sql = """
                    INSERT INTO public.temp_ingreso_proceso
                        (total_acumulado, api_estado, usu_cre, fec_cre)
                    VALUES
                        (0.00, 'CREADO', 'DBADMIN', now())
                    RETURNING nro_proceso;;
                """;

        Number numeroProceso = (Number) em.createNativeQuery(sql).getSingleResult();
        return numeroProceso.longValue();
    }

    @Transactional
    public void registrarNuevoDetalleIngresoTemporal(TempIngresoDetalleDto detalleIngresoTemp) {
        String sql = """
                    INSERT INTO public.temp_ingreso_detalle
                        (id_temp_ingreso, id_producto, id_proveedor, cantidad, costo_unitario, fecha_vencimiento, costo_total, observacion, fec_cre)
                    VALUES
                        (:idTempIngreso, :idProducto, :idProveedor, :cantidad, :costoUnitario, CAST(:fechaVencimiento AS DATE), :costoTotal, :observacion, now());
                """;

        em.createNativeQuery(sql)
                .setParameter("idTempIngreso", detalleIngresoTemp.getIdTempIngreso())
                .setParameter("idProducto", detalleIngresoTemp.getIdProducto())
                .setParameter("idProveedor", detalleIngresoTemp.getIdProveedor())
                .setParameter("cantidad", detalleIngresoTemp.getCantidad())
                .setParameter("costoUnitario", detalleIngresoTemp.getCostoUnitario())
                .setParameter("fechaVencimiento", detalleIngresoTemp.getFechaVencimiento())
                .setParameter("costoTotal", detalleIngresoTemp.getCostoTotal())
                .setParameter("observacion", detalleIngresoTemp.getObservacion())
                .executeUpdate();
    }

    public List<Long> obtenerListadoProcesosActivosIngreso() {
        String sql = """
                SELECT
                    ti.nro_proceso
                FROM
                    public.temp_ingreso_proceso ti
                WHERE
                    ti.api_estado = 'CREADO'
                ORDER BY
                    ti.nro_proceso ASC
                """;
        List<Number> filas = em.createNativeQuery(sql).getResultList();
        List<Long> idProcesos = new ArrayList<>();
        for (Number fila : filas) {
            idProcesos.add(fila.longValue());
        }
        return idProcesos;
    }

    public Optional<CompraDto> obtenerCabeceraProcesoIngresoTemporal(Long nroProceso) {
        String sql = """
                
                        SELECT 
                    id,
                    nro_proceso,
                    total_acumulado,
                    api_estado,
                    usu_cre,
                    fec_cre
                FROM 
                    public.temp_ingreso_proceso
                WHERE 
                    nro_proceso = :nroProceso
                """;

        try {
            Object[] fila = (Object[]) em.createNativeQuery(sql)
                    .setParameter("nroProceso", nroProceso)
                    .getSingleResult();

            CompraDto dto = new CompraDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).longValue() : null);
            dto.setNroProceso(fila[1] != null ? fila[1].toString() : null);
            dto.setTotalCompra(fila[2] != null ? (java.math.BigDecimal) fila[2] : java.math.BigDecimal.ZERO);
            dto.setApiEstado((String) fila[3]);
            dto.setUsuCre((String) fila[4]);
            dto.setFecCre(fila[5] != null ? fila[5].toString() : null);
            dto.setFechaCompra(String.valueOf(LocalTime.now()));
            dto.setApiTransaccion("CREAR");

            return Optional.of(dto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<TempIngresoDetalleDto> obtenerListaDetallesIngresoTemporal(Long idProcesoTemporal) {
        String sql = """
                SELECT 
                    id,
                    id_temp_ingreso,
                    id_producto,
                    id_proveedor,
                    cantidad,
                    costo_unitario,
                    fecha_vencimiento,
                    costo_total,
                    observacion,
                    fec_cre
                FROM 
                    public.temp_ingreso_detalle
                WHERE 
                    id_temp_ingreso = :idProcesoTemporal
                ORDER BY 
                    id ASC
                """;

        List<Object[]> filas = em.createNativeQuery(sql)
                .setParameter("idProcesoTemporal", idProcesoTemporal)
                .getResultList();

        List<TempIngresoDetalleDto> detalles = new ArrayList<>();

        for (Object[] fila : filas) {
            TempIngresoDetalleDto dto = new TempIngresoDetalleDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).longValue() : null);
            dto.setIdTempIngreso(fila[1] != null ? ((Number) fila[1]).longValue() : null);
            dto.setIdProducto(fila[2] != null ? ((Number) fila[2]).longValue() : null);
            dto.setIdProveedor(fila[3] != null ? ((Number) fila[3]).intValue() : null);
            dto.setCantidad(fila[4] != null ? ((Number) fila[4]).intValue() : 0);
            dto.setCostoUnitario(fila[5] != null ? (java.math.BigDecimal) fila[5] : java.math.BigDecimal.ZERO);
            dto.setFechaVencimiento(fila[6] != null ? fila[6].toString() : null);
            dto.setCostoTotal(fila[7] != null ? (java.math.BigDecimal) fila[7] : java.math.BigDecimal.ZERO);
            dto.setObservacion((String) fila[8]);
            dto.setFecCre(fila[9] != null ? fila[9].toString() : null);

            detalles.add(dto);
        }
        return detalles;
    }

    @Transactional
    public void cancelarProcesoIngreso(Long nroProceso) {
        em.createNativeQuery("DELETE FROM public.temp_ingreso_proceso WHERE nro_proceso = :nroProceso")
                .setParameter("nroProceso", nroProceso)
                .executeUpdate();
    }
}