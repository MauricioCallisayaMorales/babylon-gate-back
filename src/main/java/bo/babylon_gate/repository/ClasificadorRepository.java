package bo.babylon_gate.repository;

import bo.babylon_gate.dto.ClasificadorIdNombreDto;
import bo.babylon_gate.dto.ProductoDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ClasificadorRepository {

    @Inject
    EntityManager em;

    public List<ClasificadorIdNombreDto> listarCategorias() {
        String sql = "SELECT id, nombre FROM public.cat_categorias WHERE api_estado != 'INACTIVO' ORDER BY nombre ASC;";
        return mapearClasificador(em.createNativeQuery(sql).getResultList());
    }

    public List<ClasificadorIdNombreDto> listarMarcas() {
        String sql = "SELECT id, nombre FROM public.cat_marcas WHERE api_estado != 'INACTIVO' ORDER BY nombre ASC;";
        return mapearClasificador(em.createNativeQuery(sql).getResultList());
    }

    public List<ClasificadorIdNombreDto> listarProveedores() {
        String sql = "SELECT id, nombre FROM public.cat_proveedores WHERE api_estado != 'INACTIVO' ORDER BY nombre ASC;";
        return mapearClasificador(em.createNativeQuery(sql).getResultList());
    }

    public List<ClasificadorIdNombreDto> listarUnidades() {
        String sql = "SELECT id, nombre FROM public.cat_unidades WHERE api_estado != 'INACTIVO' ORDER BY nombre ASC;";
        return mapearClasificador(em.createNativeQuery(sql).getResultList());
    }

    private List<ClasificadorIdNombreDto> mapearClasificador(List<Object[]> filas) {
        List<ClasificadorIdNombreDto> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            ClasificadorIdNombreDto dto = new ClasificadorIdNombreDto();
            dto.setId(fila[0] != null ? ((Number) fila[0]).intValue() : null);
            dto.setNombre((String) fila[1]);
            resultado.add(dto);
        }
        return resultado;
    }

    @Transactional
    public Integer insertarCategoria(bo.babylon_gate.dto.CategoriaDto dto) {
        String sql = """
            INSERT INTO public.cat_categorias (nombre, vida_util_dias, alerta_amarilla_porc, alerta_roja_porc, api_estado, api_transaccion, usu_cre, fec_cre)
            VALUES (:nombre, :vidaUtil, :alertaAmarilla, :alertaRoja, 'CREADO', 'CREAR', 'DBADMIN', now())
            RETURNING id;
        """;
        Number idGenerado = (Number) em.createNativeQuery(sql)
                .setParameter("nombre", dto.getNombre())
                .setParameter("vidaUtil", dto.getVidaUtilDias() != null ? dto.getVidaUtilDias() : 365)
                .setParameter("alertaAmarilla", dto.getAlertaAmarillaPorc() != null ? dto.getAlertaAmarillaPorc() : 70)
                .setParameter("alertaRoja", dto.getAlertaRojaPorc() != null ? dto.getAlertaRojaPorc() : 85)
                .getSingleResult();
        return idGenerado.intValue();
    }

    @Transactional
    public Integer insertarMarca(String nombre) {
        String sql = """
            INSERT INTO public.cat_marcas (nombre, api_estado, api_transaccion, usu_cre, fec_cre)
            VALUES (:nombre, 'CREADO', 'CREAR', 'DBADMIN', now())
            RETURNING id;
        """;
        Number idGenerado = (Number) em.createNativeQuery(sql)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return idGenerado.intValue();
    }

    @Transactional
    public Integer insertarProveedor(String nombre) {
        // Nota: Como tu endpoint del Service solo recibe "nombre", mandamos dirección y teléfono nulos por ahora
        String sql = """
            INSERT INTO public.cat_proveedores (nombre, telefono, direccion, api_estado, api_transaccion, usu_cre, fec_cre)
            VALUES (:nombre, null, null, 'CREADO', 'CREAR', 'DBADMIN', now())
            RETURNING id;
        """;
        Number idGenerado = (Number) em.createNativeQuery(sql)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return idGenerado.intValue();
    }

    @Transactional
    public Integer insertarUnidad(String nombre) {
        String sql = """
            INSERT INTO public.cat_unidades (nombre, api_estado, api_transaccion, usu_cre, fec_cre)
            VALUES (:nombre, 'CREADO', 'CREAR', 'DBADMIN', now())
            RETURNING id;
        """;
        Number idGenerado = (Number) em.createNativeQuery(sql)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return idGenerado.intValue();
    }
}