package bo.babylon_gate.repository;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.utils.ImageUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductoRepository {

    @Inject
    EntityManager em;

    @Transactional
    public void insertarNuevoProducto(ProductoDto producto) {
        String letraSql = "SELECT UPPER(SUBSTRING(nombre, 1, 1)) FROM public.cat_categorias WHERE id = :idCategoria";
        String letra = (String) em.createNativeQuery(letraSql)
                .setParameter("idCategoria", producto.getIdCategoria())
                .getSingleResult();

        String maxSecSql = "SELECT COALESCE(MAX(CAST(NULLIF(SUBSTRING(codigo FROM 3), '') AS INTEGER)), 0) FROM public.productos WHERE codigo LIKE :prefijo";
        Number maxSec = (Number) em.createNativeQuery(maxSecSql)
                .setParameter("prefijo", letra + "-%")
                .getSingleResult();

        String nuevoCodigo = String.format("%s-%d", letra, maxSec.intValue() + 1);

        String sql = """
                    INSERT INTO productos (
                        codigo, detalle, id_categoria, id_marca, id_unidad,
                        precio_venta, precio_mayorista, stock_actual,
                        api_estado, api_transaccion, usu_cre, fec_cre
                    ) VALUES (
                        :codigo, :detalle, :idCategoria, :idMarca, :idUnidad,
                        :precioVenta, :precioMayorista, :stockActual,
                        'CREADO', 'CREAR', 'DBADMIN', now()
                    ) RETURNING id;
                """;

        Number idGenerado = (Number) em.createNativeQuery(sql)
                .setParameter("codigo", nuevoCodigo)
                .setParameter("detalle", producto.getDetalle())
                .setParameter("idCategoria", producto.getIdCategoria())
                .setParameter("idMarca", producto.getIdMarca())
                .setParameter("idUnidad", producto.getIdUnidad())
                .setParameter("precioVenta", producto.getPrecioVenta() != null ? producto.getPrecioVenta() : 0)
                .setParameter("precioMayorista", producto.getPrecioMayorista() != null ? producto.getPrecioMayorista() : 0)
                .setParameter("stockActual", 0)
                .getSingleResult();

        Long idProd = idGenerado.longValue();

        String sqlEmpaqueBase = """
                INSERT INTO public.producto_empaques (
                    id_producto, id_unidad, codigo_barras, cantidad_base, 
                    precio_venta, precio_mayorista, api_estado, usu_cre
                ) VALUES (
                    :idProd, :idUnidad, :codBarras, 1, 
                    :precioVenta, :precioMayorista, 'ACTIVO', 'DBADMIN'
                )
            """;
        em.createNativeQuery(sqlEmpaqueBase)
                .setParameter("idProd", idProd)
                .setParameter("idUnidad", producto.getIdUnidad())
                .setParameter("codBarras", producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "")
                .setParameter("precioVenta", producto.getPrecioVenta() != null ? producto.getPrecioVenta() : java.math.BigDecimal.ZERO)
                .setParameter("precioMayorista", producto.getPrecioMayorista() != null ? producto.getPrecioMayorista() : java.math.BigDecimal.ZERO)
                .executeUpdate();

        if (producto.getEmpaques() != null && !producto.getEmpaques().isEmpty()) {
            String sqlEmpaque = """
                        INSERT INTO producto_empaques (
                            id_producto, id_unidad, codigo_barras, cantidad_base, 
                            precio_venta, precio_mayorista, api_estado, usu_cre
                        ) VALUES (
                            :idProd, :idUnidad, :codBarras, :cantidadBase, 
                            :precioVenta, :precioMayorista, 'ACTIVO', 'DBADMIN'
                        )
                    """;
            for (ProductoEmpaqueDto emp : producto.getEmpaques()) {
                if (emp.getCantidadBase() != null && emp.getCantidadBase() >= 1) {
                    em.createNativeQuery(sqlEmpaque)
                            .setParameter("idProd", idProd)
                            .setParameter("idUnidad", emp.getIdUnidad())
                            .setParameter("codBarras", emp.getCodigoBarras() != null ? emp.getCodigoBarras() : "")
                            .setParameter("cantidadBase", emp.getCantidadBase())
                            .setParameter("precioVenta", emp.getPrecioVenta() != null ? emp.getPrecioVenta() : java.math.BigDecimal.ZERO)
                            .setParameter("precioMayorista", emp.getPrecioMayorista() != null ? emp.getPrecioMayorista() : java.math.BigDecimal.ZERO)
                            .executeUpdate();
                }
            }
        }

        if (producto.getImagenBase64() != null && !producto.getImagenBase64().isEmpty()) {
            String miniatura = ImageUtils.procesarYComprimirBase64(producto.getImagenBase64(), 100, 100);
            String estandar = ImageUtils.procesarYComprimirBase64(producto.getImagenBase64(), 600, 600);

            String sqlImg = "INSERT INTO public.producto_imagenes (id_producto, imagen_miniatura, imagen_estandar) VALUES (:idProd, :mini, :std)";
            em.createNativeQuery(sqlImg)
                    .setParameter("idProd", idProd)
                    .setParameter("mini", miniatura)
                    .setParameter("std", estandar)
                    .executeUpdate();
        }
    }

    public List<ProductoLoteDto> listarLotesActivosPorProducto(Long idProducto) {
        String sql = """
            SELECT pl.id, pl.fecha_vencimiento, pl.stock_lote_actual, pl.precio_oferta, a.nombre AS nombre_almacen 
            FROM public.producto_lotes pl 
            LEFT JOIN public.cat_almacenes a ON pl.id_almacen = a.id
            WHERE pl.id_producto = :id AND pl.api_estado = 'ACTIVO' 
            ORDER BY pl.fecha_vencimiento ASC NULLS LAST;
        """;
        List<Object[]> filas = em.createNativeQuery(sql).setParameter("id", idProducto).getResultList();
        List<ProductoLoteDto> dtos = new ArrayList<>();
        for (Object[] fila : filas) {
            ProductoLoteDto dto = new ProductoLoteDto();
            dto.setId(((Number) fila[0]).longValue());
            dto.setFechaVencimiento(fila[1] != null ? fila[1].toString() : "Sin Vencimiento");
            dto.setStockLoteActual(((Number) fila[2]).intValue());
            dto.setPrecioOferta(fila[3] != null ? (java.math.BigDecimal) fila[3] : null);
            dto.setNombreAlmacen(fila[4] != null ? (String) fila[4] : "Principal");
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public void procesarBajaDeLote(BajaLoteDto baja) {
        // 1. VALIDACIÓN ESTRICTA: Consultar el stock real del lote en este milisegundo
        Integer stockLoteReal = (Integer) em.createNativeQuery("SELECT stock_lote_actual FROM public.producto_lotes WHERE id = :idLote FOR UPDATE")
                .setParameter("idLote", baja.getIdLote())
                .getSingleResult();

        if (baja.getCantidadBaja() > stockLoteReal) {
            throw new RuntimeException("Error: Intentas dar de baja " + baja.getCantidadBaja() + " pero el lote solo tiene " + stockLoteReal + " disponibles (posiblemente se vendieron mientras editabas).");
        }

        // El resto del código se mantiene igual, tu lógica de actualizar lote, producto global y kardex es correcta.
        String sqlUpdateLote = """
                    UPDATE public.producto_lotes 
                    SET stock_lote_actual = stock_lote_actual - :cantidad, 
                        api_estado = CASE WHEN (stock_lote_actual - :cantidad) <= 0 THEN 'AGOTADO' ELSE 'ACTIVO' END,
                        usu_mod = 'DBADMIN', fec_mod = now()
                    WHERE id = :idLote;
                """;

        String sqlUpdateStock = """
                    UPDATE public.productos 
                    SET stock_actual = stock_actual - :cantidad, usu_mod = 'DBADMIN', fec_mod = now() 
                    WHERE id = :idProducto;
                """;

        String sqlKardex = """
                    INSERT INTO public.kardex_inventario (
                        id_producto, tipo_movimiento, cantidad_base_movida, saldo_stock_base, referencia_proceso, usu_cre
                    ) VALUES (
                        :idProducto, 'AJUSTE', :cantidadNegativa, 
                        (SELECT stock_actual FROM public.productos WHERE id = :idProducto), 
                        :motivo, 'DBADMIN'
                    );
                """;

        em.createNativeQuery(sqlUpdateLote)
                .setParameter("cantidad", baja.getCantidadBaja())
                .setParameter("idLote", baja.getIdLote())
                .executeUpdate();

        em.createNativeQuery(sqlUpdateStock)
                .setParameter("cantidad", baja.getCantidadBaja())
                .setParameter("idProducto", baja.getIdProducto())
                .executeUpdate();

        em.createNativeQuery(sqlKardex)
                .setParameter("idProducto", baja.getIdProducto())
                .setParameter("cantidadNegativa", -baja.getCantidadBaja())
                .setParameter("motivo", "BAJA LOTE #" + baja.getIdLote() + ": " + baja.getMotivo())
                .executeUpdate();
    }

    public List<ListaGeneralProductoDto> listarProductosCompletos() {
        String sql = """
                SELECT 
                    p.id,
                    p.codigo,
                    p.detalle,
                    c.nombre as "categoria",
                    m.nombre as "marca",
                    u.nombre as "unidad",
                    p.precio_venta as "precioVenta",
                    p.precio_mayorista as "precioMayorista",
                    (p.stock_actual - COALESCE(p.stock_reservado, 0)) as "stock_actual",
                    (SELECT pe.codigo_barras FROM producto_empaques pe WHERE pe.id_producto = p.id ORDER BY pe.cantidad_base ASC LIMIT 1) as "idCodBarras",
                    p.api_estado as "apiEstado",
                    p.api_transaccion as "apiTransaccion",
                    (SELECT pi.imagen_miniatura FROM public.producto_imagenes pi WHERE pi.id_producto = p.id) as "imagenMiniatura"
                FROM
                    productos p
                    LEFT JOIN cat_categorias c ON p.id_categoria = c.id
                    LEFT JOIN cat_marcas m ON p.id_marca = m.id
                    LEFT JOIN cat_unidades u ON p.id_unidad = u.id
                ORDER BY
                    p.id DESC
                """;
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        List<ListaGeneralProductoDto> dtos = new ArrayList<>();

        for (Object[] fila : filas) {
            ListaGeneralProductoDto dto = new ListaGeneralProductoDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).longValue() : null);
            dto.setCodigo((String) fila[1]);
            dto.setDetalle((String) fila[2]);
            dto.setCategoria((String) fila[3]);
            dto.setMarca((String) fila[4]);
            dto.setUnidad((String) fila[5]);
            dto.setPrecioVenta(fila[6] != null ? (java.math.BigDecimal) fila[6] : java.math.BigDecimal.ZERO);
            dto.setPrecioMayorista(fila[7] != null ? (java.math.BigDecimal) fila[7] : java.math.BigDecimal.ZERO);
            dto.setStockActual(fila[8] != null ? ((Number) fila[8]).intValue() : 0);
            dto.setIdCodBarras((String) fila[9]);
            dto.setApiEstado((String) fila[10]);
            dto.setApiTransaccion((String) fila[11]);
            dto.setImagenMiniatura((String) fila[12]);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<ProductoInventarioDto> listarProductosParaInventario() {
        String sql = """
                    SELECT 
                        p.id, p.codigo, p.detalle, c.nombre as "categoria", m.nombre as "marca", u.nombre as "unidad", 
                        p.precio_venta, p.precio_mayorista,
                        (p.stock_actual - COALESCE(p.stock_reservado, 0)) as "stock_actual",
                        (SELECT MAX(cd.fec_cre) FROM compras_detalle cd WHERE cd.id_producto = p.id AND cd.api_transaccion = 'CREAR') as "fechaIngreso",
                        (SELECT MIN(cd.fecha_vencimiento) FROM compras_detalle cd WHERE cd.id_producto = p.id AND cd.fecha_vencimiento >= CURRENT_DATE AND cd.api_transaccion = 'CREAR') as "fechaVencimiento",
                        COALESCE(c.vida_util_dias, 365), 
                        COALESCE(c.alerta_amarilla_porc, 70), 
                        COALESCE(c.alerta_roja_porc, 85),
                        (SELECT pi.imagen_miniatura FROM public.producto_imagenes pi WHERE pi.id_producto = p.id) as "imagenMiniatura"
                    FROM productos p
                    LEFT JOIN cat_categorias c ON p.id_categoria = c.id
                    LEFT JOIN cat_marcas m ON p.id_marca = m.id
                    LEFT JOIN cat_unidades u ON p.id_unidad = u.id
                    WHERE p.api_estado != 'INACTIVO'
                    ORDER BY "fechaVencimiento" ASC;
                """;
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        List<ProductoInventarioDto> dtos = new ArrayList<>();

        for (Object[] fila : filas) {
            ProductoInventarioDto dto = new ProductoInventarioDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).longValue() : null);
            dto.setCodigo((String) fila[1]);
            dto.setDetalle((String) fila[2]);
            dto.setCategoria((String) fila[3]);
            dto.setMarca((String) fila[4]);
            dto.setUnidad((String) fila[5]);
            dto.setPrecioVenta(fila[6] != null ? (java.math.BigDecimal) fila[6] : java.math.BigDecimal.ZERO);
            dto.setPrecioMayorista(fila[7] != null ? (java.math.BigDecimal) fila[7] : java.math.BigDecimal.ZERO);
            dto.setCantidad(fila[8] != null ? ((Number) fila[8]).intValue() : 0);
            dto.setFechaIngreso(fila[9] != null ? fila[9].toString() : null);
            dto.setFechaVencimiento(fila[10] != null ? fila[10].toString() : null);
            dto.setVidaUtilDias(fila[11] != null ? ((Number) fila[11]).intValue() : 365);
            dto.setAlertaAmarillaPorc(fila[12] != null ? ((Number) fila[12]).intValue() : 70);
            dto.setAlertaRojaPorc(fila[13] != null ? ((Number) fila[13]).intValue() : 85);
            dto.setImagenMiniatura((String) fila[14]);
            dtos.add(dto);
        }
        return dtos;
    }

    public Optional<ProductoScannerDto> buscarPorCodigoBarrasEnBD(String codigoBarras) {
        String sql = """
                SELECT
                    p.id,
                    p.codigo,
                    pe.codigo_barras,
                    p.detalle,
                    c.nombre,
                    m.nombre,
                    u.nombre,
                    pe.precio_venta,
                    pe.precio_mayorista,
                    p.stock_actual
                FROM
                    productos p
                    LEFT JOIN producto_empaques pe ON p.id = pe.id_producto
                    LEFT JOIN cat_categorias c ON p.id_categoria = c.id
                    LEFT JOIN cat_marcas m ON p.id_marca = m.id
                    LEFT JOIN cat_unidades u ON pe.id_unidad = u.id
                WHERE
                    pe.codigo_barras = :cb OR p.codigo = :cb
                """;
        try {
            Object[] fila = (Object[]) em.createNativeQuery(sql)
                    .setParameter("cb", codigoBarras)
                    .getSingleResult();

            ProductoScannerDto dto = new ProductoScannerDto();
            dto.setExisteEnSistema(true);
            dto.setId(fila[0] != null ? ((Number) fila[0]).intValue() : null);
            dto.setCodigo((String) fila[1]);
            dto.setIdCodBarras((String) fila[2]);
            dto.setDetalle((String) fila[3]);
            dto.setCategoria((String) fila[4]);
            dto.setMarca((String) fila[5]);
            dto.setUnidad((String) fila[6]);
            dto.setPrecioVenta(fila[7] != null ? (java.math.BigDecimal) fila[7] : java.math.BigDecimal.ZERO);
            dto.setPrecioMayorista(fila[8] != null ? (java.math.BigDecimal) fila[8] : java.math.BigDecimal.ZERO);
            dto.setStockActual(fila[9] != null ? ((Number) fila[9]).intValue() : 0);

            return Optional.of(dto);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void actualizarRegistroProducto(ProductoDto producto) {
        if (producto.getId() == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio");
        }

        Integer catActual = (Integer) em.createNativeQuery("SELECT id_categoria FROM public.productos WHERE id = :id")
                .setParameter("id", producto.getId()).getSingleResult();
        String codigoActual = (String) em.createNativeQuery("SELECT codigo FROM public.productos WHERE id = :id")
                .setParameter("id", producto.getId()).getSingleResult();
        producto.setCodigo(codigoActual);

        if (!catActual.equals(producto.getIdCategoria())) {
            String letraSql = "SELECT UPPER(SUBSTRING(nombre, 1, 1)) FROM public.cat_categorias WHERE id = :idCategoria";
            String letra = (String) em.createNativeQuery(letraSql)
                    .setParameter("idCategoria", producto.getIdCategoria())
                    .getSingleResult();

            String maxSecSql = "SELECT COALESCE(MAX(CAST(NULLIF(SUBSTRING(codigo FROM 3), '') AS INTEGER)), 0) FROM public.productos WHERE codigo LIKE :prefijo";
            Number maxSec = (Number) em.createNativeQuery(maxSecSql)
                    .setParameter("prefijo", letra + "-%")
                    .getSingleResult();

            String nuevoCodigo = String.format("%s-%d", letra, maxSec.intValue() + 1);
            producto.setCodigo(nuevoCodigo);
        }

        String sqlUpdateProducto = """
                    UPDATE public.productos
                    SET
                        codigo= :codigo,
                        detalle= :detalle,
                        id_categoria=:idCategoria,
                        id_marca= :idMarca,
                        id_unidad= :idUnidad,
                        precio_venta= :precioVenta,
                        precio_mayorista= :precioMayorista,
                        usu_mod='DBADMIN',
                        fec_mod=now()
                    WHERE id= :id;
                """;

        int filas = em.createNativeQuery(sqlUpdateProducto)
                .setParameter("codigo", producto.getCodigo())
                .setParameter("detalle", producto.getDetalle())
                .setParameter("idCategoria", producto.getIdCategoria())
                .setParameter("idMarca", producto.getIdMarca())
                .setParameter("idUnidad", producto.getIdUnidad())
                .setParameter("precioVenta", producto.getPrecioVenta())
                .setParameter("precioMayorista", producto.getPrecioMayorista())
                .setParameter("id", producto.getId())
                .executeUpdate();

        if (filas == 0) {
            throw new RuntimeException("Producto no encontrado");
        }

        if (producto.getProveedores() != null) {

            List<Integer> idsProveedoresActivos = new ArrayList<>();
            for (ProductoProveedorDto prov : producto.getProveedores()) {
                if (prov.getIdProveedor() != null) {
                    idsProveedoresActivos.add(prov.getIdProveedor());
                }
            }

            if (!idsProveedoresActivos.isEmpty()) {
                String sqlDeleteRemoved = """
                            DELETE FROM public.producto_proveedor 
                            WHERE id_producto = :idProducto 
                            AND id_proveedor NOT IN :idsActivos
                        """;
                em.createNativeQuery(sqlDeleteRemoved)
                        .setParameter("idProducto", producto.getId())
                        .setParameter("idsActivos", idsProveedoresActivos)
                        .executeUpdate();
            } else {
                em.createNativeQuery("DELETE FROM public.producto_proveedor WHERE id_producto = :idProducto")
                        .setParameter("idProducto", producto.getId())
                        .executeUpdate();
            }

            em.createNativeQuery("DELETE FROM public.producto_empaques WHERE id_producto = :idProd")
                    .setParameter("idProd", producto.getId())
                    .executeUpdate();

            String sqlEmpaqueBase = """
                    INSERT INTO public.producto_empaques (
                        id_producto, id_unidad, codigo_barras, cantidad_base, 
                        precio_venta, precio_mayorista, api_estado, usu_cre
                    ) VALUES (
                        :idProd, :idUnidad, :codBarras, 1, 
                        :precioVenta, :precioMayorista, 'ACTIVO', 'DBADMIN'
                    )
                """;
            em.createNativeQuery(sqlEmpaqueBase)
                    .setParameter("idProd", producto.getId())
                    .setParameter("idUnidad", producto.getIdUnidad())
                    .setParameter("codBarras", producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "")
                    .setParameter("precioVenta", producto.getPrecioVenta() != null ? producto.getPrecioVenta() : java.math.BigDecimal.ZERO)
                    .setParameter("precioMayorista", producto.getPrecioMayorista() != null ? producto.getPrecioMayorista() : java.math.BigDecimal.ZERO)
                    .executeUpdate();

            if (producto.getEmpaques() != null && !producto.getEmpaques().isEmpty()) {
                String sqlEmpaque = """
                            INSERT INTO public.producto_empaques (
                                id_producto, id_unidad, codigo_barras, cantidad_base, 
                                precio_venta, precio_mayorista, api_estado, usu_cre
                            ) VALUES (
                                :idProd, :idUnidad, :codBarras, :cantidadBase, 
                                :precioVenta, :precioMayorista, 'ACTIVO', 'DBADMIN'
                            )
                        """;
                for (ProductoEmpaqueDto emp : producto.getEmpaques()) {
                    if (emp.getCantidadBase() != null && emp.getCantidadBase() >= 1) {
                        em.createNativeQuery(sqlEmpaque)
                                .setParameter("idProd", producto.getId())
                                .setParameter("idUnidad", emp.getIdUnidad())
                                .setParameter("codBarras", emp.getCodigoBarras() != null ? emp.getCodigoBarras() : "")
                                .setParameter("cantidadBase", emp.getCantidadBase())
                                .setParameter("precioVenta", emp.getPrecioVenta() != null ? emp.getPrecioVenta() : java.math.BigDecimal.ZERO)
                                .setParameter("precioMayorista", emp.getPrecioMayorista() != null ? emp.getPrecioMayorista() : java.math.BigDecimal.ZERO)
                                .executeUpdate();
                    }
                }
            }

            String sqlUpsertProveedor = """
                        INSERT INTO public.producto_proveedor (
                            id_producto, id_proveedor, id_unidad, costo_referencial, 
                            codigo_proveedor, proveedor_principal
                        ) VALUES (
                            :idProducto, :idProveedor, :idUnidad, :costoReferencial, 
                            :codigoProveedor, :proveedorPrincipal
                        )
                        ON CONFLICT (id_producto, id_proveedor, id_unidad) 
                        DO UPDATE SET 
                            costo_referencial = EXCLUDED.costo_referencial,
                            codigo_proveedor = EXCLUDED.codigo_proveedor,
                            proveedor_principal = EXCLUDED.proveedor_principal;
                    """;

            for (ProductoProveedorDto prov : producto.getProveedores()) {
                em.createNativeQuery(sqlUpsertProveedor)
                        .setParameter("idProducto", producto.getId())
                        .setParameter("idProveedor", prov.getIdProveedor())
                        .setParameter("idUnidad", prov.getIdUnidad())
                        .setParameter("costoReferencial", prov.getCostoReferencial() != null ? prov.getCostoReferencial() : java.math.BigDecimal.ZERO)
                        .setParameter("codigoProveedor", prov.getCodigoProveedor())
                        .setParameter("proveedorPrincipal", prov.getProveedorPrincipal() != null ? prov.getProveedorPrincipal() : false)
                        .executeUpdate();
            }

            if (producto.getImagenBase64() != null && !producto.getImagenBase64().isEmpty()) {
                String miniatura = ImageUtils.procesarYComprimirBase64(producto.getImagenBase64(), 100, 100);
                String estandar = ImageUtils.procesarYComprimirBase64(producto.getImagenBase64(), 600, 600);

                String sqlImgUpsert = """
                INSERT INTO public.producto_imagenes (id_producto, imagen_miniatura, imagen_estandar) 
                VALUES (:idProd, :mini, :std)
                ON CONFLICT (id_producto) DO UPDATE SET 
                    imagen_miniatura = EXCLUDED.imagen_miniatura,
                    imagen_estandar = EXCLUDED.imagen_estandar;
            """;
                em.createNativeQuery(sqlImgUpsert)
                        .setParameter("idProd", producto.getId())
                        .setParameter("mini", miniatura)
                        .setParameter("std", estandar)
                        .executeUpdate();
            }
        }
    }

    @Transactional
    public void asociarCodigoBarras(Long idProducto, String codigoBarras) {
        String sqlUnidad = "SELECT id_unidad, precio_venta, precio_mayorista FROM public.productos WHERE id = :id";
        Object[] prodData = (Object[]) em.createNativeQuery(sqlUnidad)
                .setParameter("id", idProducto)
                .getSingleResult();

        String sql = """
                    INSERT INTO public.producto_empaques (
                        id_producto, id_unidad, codigo_barras, cantidad_base, 
                        precio_venta, precio_mayorista, api_estado, usu_cre
                    ) VALUES (
                        :idProd, :idUnidad, :cb, 1, 
                        :precioV, :precioM, 'ACTIVO', 'DBADMIN'
                    )
                """;
        em.createNativeQuery(sql)
                .setParameter("idProd", idProducto)
                .setParameter("idUnidad", ((Number) prodData[0]).intValue())
                .setParameter("cb", codigoBarras)
                .setParameter("precioV", prodData[1])
                .setParameter("precioM", prodData[2])
                .executeUpdate();
    }

    public List<ProductoEmpaqueDto> listarEmpaquesPorProducto(Long idProducto) {
        String sql = """
                    SELECT pe.id_unidad, u.nombre, pe.codigo_barras, pe.cantidad_base, pe.precio_venta, pe.precio_mayorista
                    FROM public.producto_empaques pe
                    JOIN public.cat_unidades u ON pe.id_unidad = u.id
                    WHERE pe.id_producto = :id
                    ORDER BY pe.cantidad_base ASC
                """;
        List<Object[]> filas = em.createNativeQuery(sql).setParameter("id", idProducto).getResultList();
        List<ProductoEmpaqueDto> dtos = new ArrayList<>();

        for (Object[] fila : filas) {
            ProductoEmpaqueDto dto = new ProductoEmpaqueDto();
            dto.setIdUnidad(((Number) fila[0]).intValue());
            dto.setUnidadNombre((String) fila[1]);
            dto.setCodigoBarras((String) fila[2]);
            dto.setCantidadBase(((Number) fila[3]).intValue());
            dto.setPrecioVenta(fila[4] != null ? (java.math.BigDecimal) fila[4] : null);
            dto.setPrecioMayorista(fila[5] != null ? (java.math.BigDecimal) fila[5] : null);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<ProductoEmpaqueDto> listarTodosLosEmpaques() {
        String sql = """
                    SELECT pe.id_producto, pe.id_unidad, u.nombre, pe.codigo_barras, pe.cantidad_base, pe.precio_venta, pe.precio_mayorista
                    FROM public.producto_empaques pe
                    JOIN public.cat_unidades u ON pe.id_unidad = u.id
                    WHERE pe.api_estado = 'ACTIVO'
                    ORDER BY pe.id_producto ASC, pe.cantidad_base DESC
                """;
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        List<ProductoEmpaqueDto> dtos = new ArrayList<>();

        for (Object[] fila : filas) {
            ProductoEmpaqueDto dto = new ProductoEmpaqueDto();
            dto.setIdProducto(((Number) fila[0]).longValue());
            dto.setIdUnidad(((Number) fila[1]).intValue());
            dto.setUnidadNombre((String) fila[2]);
            dto.setCodigoBarras((String) fila[3]);
            dto.setCantidadBase(((Number) fila[4]).intValue());
            dto.setPrecioVenta(fila[5] != null ? (java.math.BigDecimal) fila[5] : null);
            dto.setPrecioMayorista(fila[6] != null ? (java.math.BigDecimal) fila[6] : null);
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public void cambiarEstadoProducto(Long idProducto, String nuevoEstado) {
        String sql = "UPDATE public.productos SET api_estado = :estado, usu_mod = 'DBADMIN', fec_mod = now() WHERE id = :id";
        int filas = em.createNativeQuery(sql)
                .setParameter("estado", nuevoEstado)
                .setParameter("id", idProducto)
                .executeUpdate();

        if (filas == 0) {
            throw new RuntimeException("No se encontró el producto para actualizar su estado");
        }
    }

    public List<ProductoProveedorDto> listarProveedoresPorProducto(Long idProducto) {
        String sql = """
                SELECT id_proveedor, id_unidad, costo_referencial, codigo_proveedor, proveedor_principal 
                FROM public.producto_proveedor 
                WHERE id_producto = :id
                """;
        List<Object[]> filas = em.createNativeQuery(sql).setParameter("id", idProducto).getResultList();
        List<ProductoProveedorDto> dtos = new ArrayList<>();

        for (Object[] fila : filas) {
            ProductoProveedorDto dto = new ProductoProveedorDto();
            dto.setIdProveedor((Integer) fila[0]);
            dto.setIdUnidad((Integer) fila[1]);
            dto.setCostoReferencial(fila[2] != null ? (java.math.BigDecimal) fila[2] : java.math.BigDecimal.ZERO);
            dto.setCodigoProveedor((String) fila[3]);
            dto.setProveedorPrincipal((Boolean) fila[4]);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<KardexDto> obtenerKardexInventario(Long idProducto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT k.id, k.fecha_movimiento, p.detalle, k.tipo_movimiento, k.cantidad_base_movida, k.saldo_stock_base, k.referencia_proceso 
                    FROM public.kardex_inventario k 
                    JOIN public.productos p ON k.id_producto = p.id 
                """);

        if (idProducto != null) {
            sql.append("WHERE k.id_producto = :idProducto ");
        }

        sql.append("ORDER BY k.fecha_movimiento DESC ");

        if (idProducto == null) {
            sql.append("LIMIT 500");
        }

        var query = em.createNativeQuery(sql.toString());
        if (idProducto != null) {
            query.setParameter("idProducto", idProducto);
        }

        List<Object[]> filas = query.getResultList();
        List<KardexDto> lista = new ArrayList<>();

        for (Object[] f : filas) {
            KardexDto dto = new KardexDto();
            dto.setId(((Number) f[0]).longValue());
            dto.setFechaMovimiento(f[1] != null ? f[1].toString() : "");
            dto.setNombreProducto((String) f[2]);
            dto.setTipoMovimiento((String) f[3]);
            dto.setCantidad(((Number) f[4]).intValue());
            dto.setSaldoActual(((Number) f[5]).intValue());
            dto.setReferencia((String) f[6]);
            lista.add(dto);
        }
        return lista;
    }

    @Transactional
    public void actualizarPreciosBase(Long idProducto, java.math.BigDecimal precioVenta, java.math.BigDecimal precioMayorista) {
        em.createNativeQuery("UPDATE public.productos SET precio_venta = :pv, precio_mayorista = :pm, usu_mod = 'DBADMIN', fec_mod = now() WHERE id = :id")
                .setParameter("pv", precioVenta)
                .setParameter("pm", precioMayorista)
                .setParameter("id", idProducto).executeUpdate();

        em.createNativeQuery("UPDATE public.producto_empaques SET precio_venta = :pv, precio_mayorista = :pm WHERE id_producto = :id AND cantidad_base = 1")
                .setParameter("pv", precioVenta)
                .setParameter("pm", precioMayorista)
                .setParameter("id", idProducto).executeUpdate();
    }

    @Transactional
    public void aplicarOfertaLote(Long idLote, java.math.BigDecimal precioOferta) {
        em.createNativeQuery("UPDATE public.producto_lotes SET precio_oferta = :precio, usu_mod = 'DBADMIN', fec_mod = now() WHERE id = :idLote")
                .setParameter("precio", precioOferta)
                .setParameter("idLote", idLote)
                .executeUpdate();
    }

    public List<Object[]> listarAlmacenesActivos() {
        return em.createNativeQuery("SELECT id, nombre FROM public.cat_almacenes WHERE api_estado = 'ACTIVO' ORDER BY id ASC").getResultList();
    }

    @Transactional
    public void trasladarLoteDeAlmacen(Long idLoteOrigen, Integer cantidad, Integer idAlmacenDestino) {
        String sqlLote = "SELECT id_producto, stock_lote_actual, id_almacen, fecha_vencimiento, precio_oferta, id_compra_detalle FROM public.producto_lotes WHERE id = :id FOR UPDATE";
        Object[] loteOriginal = (Object[]) em.createNativeQuery(sqlLote).setParameter("id", idLoteOrigen).getSingleResult();

        int stockActual = ((Number) loteOriginal[1]).intValue();
        int idAlmacenOrigen = ((Number) loteOriginal[2]).intValue();
        Long idProducto = ((Number) loteOriginal[0]).longValue();

        if (cantidad > stockActual) throw new RuntimeException("Stock insuficiente para trasladar.");
        if (idAlmacenOrigen == idAlmacenDestino) throw new RuntimeException("El almacén destino debe ser diferente.");

        em.createNativeQuery("UPDATE public.producto_lotes SET stock_lote_actual = stock_lote_actual - :cant, api_estado = CASE WHEN (stock_lote_actual - :cant) <= 0 THEN 'AGOTADO' ELSE 'ACTIVO' END WHERE id = :id")
                .setParameter("cant", cantidad).setParameter("id", idLoteOrigen).executeUpdate();

        String insertLote = """
            INSERT INTO public.producto_lotes (id_producto, fecha_vencimiento, stock_lote_actual, precio_oferta, id_compra_detalle, id_almacen, api_estado, usu_cre)
            VALUES (:idProd, CAST(:fechaV AS date), :cant, :precioO, :idCompra, :idAlm, 'ACTIVO', 'DBADMIN')
        """;
        var query = em.createNativeQuery(insertLote)
                .setParameter("idProd", idProducto)
                .setParameter("cant", cantidad)
                .setParameter("precioO", loteOriginal[4])
                .setParameter("idCompra", loteOriginal[5])
                .setParameter("idAlm", idAlmacenDestino);

        if (loteOriginal[3] != null) query.setParameter("fechaV", loteOriginal[3].toString());
        else query.setParameter("fechaV", null); // Manejar fecha nula
        query.executeUpdate();

        String sqlTraslado = "INSERT INTO public.traslados_almacen (id_producto, id_lote_origen, id_almacen_origen, id_almacen_destino, cantidad) VALUES (:idProd, :idLoteO, :idAlmO, :idAlmD, :cant)";
        em.createNativeQuery(sqlTraslado)
                .setParameter("idProd", idProducto).setParameter("idLoteO", idLoteOrigen)
                .setParameter("idAlmO", idAlmacenOrigen).setParameter("idAlmD", idAlmacenDestino)
                .setParameter("cant", cantidad).executeUpdate();
    }

    public String obtenerImagenEstandar(Long idProducto) {
        String sql = "SELECT imagen_estandar FROM public.producto_imagenes WHERE id_producto = :id";
        try {
            return (String) em.createNativeQuery(sql).setParameter("id", idProducto).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}