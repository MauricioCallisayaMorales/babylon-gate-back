package bo.babylon_gate.service;

import bo.babylon_gate.dto.ClasificadorIdNombreDto;
import bo.babylon_gate.dto.ProductoDto;
import bo.babylon_gate.facade.ClasificadorFacade;
import bo.babylon_gate.facade.ProductoFacade;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/clasificador")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClasificadorService {

    @Inject
    ClasificadorFacade clasificadorFacade;
    @Inject
    EntityManager em;

    @GET
    @Path("/lista/categorias")
    public List<ClasificadorIdNombreDto> getCategorias() { return clasificadorFacade.listarCategoriasProducto(); }

    @GET
    @Path("/lista/marcas")
    public List<ClasificadorIdNombreDto> getMarcas() { return clasificadorFacade.listarMarcasProducto(); }

    @GET
    @Path("/lista/proveedores")
    public List<ClasificadorIdNombreDto> getProveedores() { return clasificadorFacade.listarProveedoresProducto(); }

    @GET
    @Path("/lista/unidades")
    public List<ClasificadorIdNombreDto> getUnidades() { return clasificadorFacade.listarUnidadesVentaProducto(); }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/crear/categorias")
    public Integer saveCategoria(bo.babylon_gate.dto.CategoriaDto dto) {
        return clasificadorFacade.crearCategoria(dto);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/crear/marcas")
    public Integer saveMarca(String nombre) { return clasificadorFacade.crearMarca(nombre); }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/crear/proveedores")
    public Integer saveProveedor(String nombre) { return clasificadorFacade.crearProveedor(nombre); }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/crear/unidades")
    public Integer saveUnidad(String nombre) { return clasificadorFacade.crearUnidad(nombre); }

    @PUT
    @Path("/desactivar/{tipo}/{id}")
    public Response desactivarClasificador(@PathParam("tipo") String tipo, @PathParam("id") Long id) {
        String tabla = switch (tipo.toLowerCase()) {
            case "categorias" -> "cat_categorias";
            case "marcas" -> "cat_marcas";
            case "proveedores" -> "cat_proveedores";
            case "unidades" -> "cat_unidades";
            default -> null;
        };
        if (tabla == null) return Response.status(400).entity("Tipo no válido").build();

        String sql = "UPDATE public." + tabla + " SET api_estado = 'INACTIVO' WHERE id = :id";
        em.createNativeQuery(sql).setParameter("id", id).executeUpdate();
        return Response.ok("Registro eliminado correctamente").build();
    }
}