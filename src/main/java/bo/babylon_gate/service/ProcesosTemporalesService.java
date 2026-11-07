package bo.babylon_gate.service;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.facade.ProcesosTemporalesFacade;
import bo.babylon_gate.facade.ProductoFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/procesos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcesosTemporalesService {

    @Inject
    ProcesosTemporalesFacade procesosTemporalesFacade;

    @POST
    @Path("/crear/ingreso")
    public Response crearProcesoIngreso() {
        try {
            Long procesoCreado = procesosTemporalesFacade.registrarNuevoIngresoTemporal();
            return Response.status(Response.Status.CREATED).entity(procesoCreado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/crear/detalle-ingreso")
    public Response crearDetalleProcesoIngreso(TempIngresoDetalleDto detalleIngresoTemp) {
        try {
            procesosTemporalesFacade.registrarNuevoDetalleIngresoTemporal(detalleIngresoTemp);
            return Response.status(Response.Status.CREATED).entity("Registro de detalle de proceso de ingreso creado correctamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/consultar/lista/ingreso-temporal")
    public Response obtenerListadoProcesosActivosIngreso() {
        try {
            List<Long> listaIngresosActivos = procesosTemporalesFacade.obtenerListadoProcesosActivosIngreso();
            return Response.status(Response.Status.CREATED).entity(listaIngresosActivos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/consultar/detalles/{idProceso}")
    public Response obtenerDetallesProceso(@PathParam("idProceso") Long idProceso) {
        try {
            List<TempIngresoDetalleDto> detalles = procesosTemporalesFacade.obtenerListaDetallesIngresoTemporal(idProceso);
            return Response.status(Response.Status.OK).entity(detalles).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/guardar/ingreso-oficial")
    public Response guardarIngresoDesdeTemporal() {
        try {
            procesosTemporalesFacade.obtenerListadoProcesosActivosIngreso();
            return Response.status(Response.Status.OK).entity("Registro oficial de ingreso guardado con éxito").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/cancelar/ingreso/{nroProceso}")
    public Response cancelarProcesoIngreso(@PathParam("nroProceso") Long nroProceso) {
        try {
            procesosTemporalesFacade.cancelarProcesoIngreso(nroProceso);
            return Response.ok("Proceso cancelado exitosamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}