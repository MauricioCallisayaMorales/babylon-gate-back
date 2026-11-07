package bo.babylon_gate.service;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.facade.ProductoFacade;
import bo.babylon_gate.facade.ProductoProveedorFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;

@Path("/producto-proveedor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoProveedorService {
    @Inject
    ProductoProveedorFacade facade;

    @GET
    @Path("/costo/{idProducto}/{idProveedor}/{idUnidad}")
    public Response obtenerCosto(@PathParam("idProducto") Long idProducto, @PathParam("idProveedor") Integer idProveedor, @PathParam("idUnidad") Integer idUnidad) {
        BigDecimal costo = facade.obtenerCostoReferencial(idProducto, idProveedor, idUnidad);
        if (costo != null) {
            return Response.ok(costo).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build(); // 204 si no existe relación
    }

    @POST
    @Path("/crear")
    public Response crearRelacion(ProductoProveedorDto dto) {
        facade.crearRelacion(dto);
        return Response.status(Response.Status.CREATED).entity("Relación creada").build();
    }

    @GET
    @Path("/comparativa/producto/{idProducto}")
    public Response compararProducto(@PathParam("idProducto") Long idProducto) {
        return Response.ok(facade.compararPorProducto(idProducto)).build();
    }

    @GET
    @Path("/comparativa/proveedor/{idProveedor}")
    public Response compararProveedor(@PathParam("idProveedor") Integer idProveedor) {
        return Response.ok(facade.compararPorProveedor(idProveedor)).build();
    }
}