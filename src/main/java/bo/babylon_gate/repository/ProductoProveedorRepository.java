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
public class ProductoProveedorRepository {

    @Inject
    EntityManager em;

    public Optional<BigDecimal> obtenerCostoReferencial(Long idProducto, Integer idProveedor, Integer idUnidad) {
        String sql = "SELECT costo_referencial FROM public.producto_proveedor WHERE id_producto = :idProd AND id_proveedor = :idProv AND id_unidad = :idUni";
        try {
            Number costo = (Number) em.createNativeQuery(sql)
                    .setParameter("idProd", idProducto)
                    .setParameter("idProv", idProveedor)
                    .setParameter("idUni", idUnidad)
                    .getSingleResult();
            return Optional.of(new java.math.BigDecimal(costo.toString()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void crearRelacion(ProductoProveedorDto dto) {
        String sql = """
            INSERT INTO public.producto_proveedor (id_producto, id_proveedor, id_unidad, costo_referencial, proveedor_principal) 
            VALUES (:idProd, :idProv, :idUni, :costo, false)
            ON CONFLICT (id_producto, id_proveedor, id_unidad)
            DO UPDATE SET costo_referencial = EXCLUDED.costo_referencial;
        """;
        em.createNativeQuery(sql)
                .setParameter("idProd", dto.getIdProducto())
                .setParameter("idProv", dto.getIdProveedor())
                .setParameter("idUni", dto.getIdUnidad())
                .setParameter("costo", dto.getCostoReferencial())
                .executeUpdate();
    }

    public List<ComparativaCostoDto> listarComparativaPorProducto(Long idProducto) {
        String sql = """
            SELECT pp.id_producto, p.detalle, pp.id_proveedor, cp.nombre as proveedor, pp.id_unidad, cu.nombre as unidad, pp.costo_referencial
            FROM public.producto_proveedor pp
            JOIN public.productos p ON pp.id_producto = p.id
            JOIN public.cat_proveedores cp ON pp.id_proveedor = cp.id
            JOIN public.cat_unidades cu ON pp.id_unidad = cu.id
            WHERE pp.id_producto = :idProd
        """;
        return mapearComparativa(em.createNativeQuery(sql).setParameter("idProd", idProducto).getResultList());
    }

    public List<ComparativaCostoDto> listarComparativaPorProveedor(Integer idProveedor) {
        String sql = """
            SELECT pp.id_producto, p.detalle, pp.id_proveedor, cp.nombre as proveedor, pp.id_unidad, cu.nombre as unidad, pp.costo_referencial
            FROM public.producto_proveedor pp
            JOIN public.productos p ON pp.id_producto = p.id
            JOIN public.cat_proveedores cp ON pp.id_proveedor = cp.id
            JOIN public.cat_unidades cu ON pp.id_unidad = cu.id
            WHERE pp.id_proveedor = :idProv
            ORDER BY p.detalle ASC, cu.nombre ASC
        """;
        return mapearComparativa(em.createNativeQuery(sql).setParameter("idProv", idProveedor).getResultList());
    }

    private List<ComparativaCostoDto> mapearComparativa(List<Object[]> filas) {
        List<ComparativaCostoDto> lista = new ArrayList<>();
        for (Object[] f : filas) {
            ComparativaCostoDto dto = new ComparativaCostoDto();
            dto.setIdProducto(f[0] != null ? ((Number) f[0]).longValue() : null);
            dto.setNombreProducto((String) f[1]);
            dto.setIdProveedor(f[2] != null ? ((Number) f[2]).intValue() : null);
            dto.setNombreProveedor((String) f[3]);
            dto.setIdUnidad(f[4] != null ? ((Number) f[4]).intValue() : null);
            dto.setNombreUnidad((String) f[5]);
            dto.setCosto(f[6] != null ? (java.math.BigDecimal) f[6] : java.math.BigDecimal.ZERO);
            lista.add(dto);
        }
        return lista;
    }
}