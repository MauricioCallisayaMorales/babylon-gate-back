package bo.babylon_gate.service;

import bo.babylon_gate.dto.TempIngresoDetalleDto;
import bo.babylon_gate.dto.TempVentaDetalleDto;
import bo.babylon_gate.facade.ProcesosTemporalesFacade;
import bo.babylon_gate.facade.ProcesosTemporalesVentaFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/procesos-venta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcesosTemporalesVentaService {

    @Inject
    ProcesosTemporalesVentaFacade facade;

    @POST
    @Path("/crear/venta")
    public Response crearProcesoVenta() {
        try {
            Long procesoCreado = facade.registrarNuevaVentaTemporal();
            return Response.status(Response.Status.CREATED).entity(procesoCreado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/crear/detalle-venta")
    public Response crearDetalleProcesoVenta(TempVentaDetalleDto detalle) {
        try {
            facade.registrarNuevoDetalleVentaTemporal(detalle);
            return Response.status(Response.Status.CREATED).entity("Detalle de venta temporal creado").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/consultar/lista/activos")
    public Response obtenerListadoProcesosActivos() {
        try {
            List<Long> activos = facade.obtenerListadoProcesosActivosVenta();
            return Response.status(Response.Status.OK).entity(activos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/consultar/detalles/{idProceso}")
    public Response obtenerDetallesProceso(@PathParam("idProceso") Long idProceso) {
        try {
            List<TempVentaDetalleDto> detalles = facade.obtenerListaDetallesVentaTemporal(idProceso);
            return Response.status(Response.Status.OK).entity(detalles).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/cancelar/venta/{nroProceso}")
    public Response cancelarProcesoVenta(@PathParam("nroProceso") Long nroProceso) {
        try {
            facade.cancelarProcesoVenta(nroProceso);
            return Response.ok("Proceso cancelado exitosamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/eliminar/detalle-venta/{idDetalle}")
    public Response eliminarDetalleVenta(@PathParam("idDetalle") Long idDetalle) {
        try {
            facade.eliminarDetalleVentaTemporal(idDetalle);
            return Response.ok("Producto removido de la cuenta").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/actualizar/detalle-venta")
    public Response actualizarDetalleVenta(TempVentaDetalleDto detalle) {
        try {
            facade.actualizarPrecioDetalleVentaTemporal(detalle.getId(), detalle.getPrecioTotal(), detalle.getPrecioUnitario());
            return Response.ok("Precio actualizado").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}