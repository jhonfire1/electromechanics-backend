package cl.electromechanics.operations.service;

import cl.electromechanics.operations.dto.OrdenDtos.Crear;
import cl.electromechanics.operations.entity.OrdenTrabajo;
import cl.electromechanics.operations.repository.OrdenTrabajoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class OrdenTrabajoService {
    private final OrdenTrabajoRepository ordenes;
    private final TecnicosClient tecnicos;

    public OrdenTrabajoService(OrdenTrabajoRepository ordenes, TecnicosClient tecnicos) {
        this.ordenes = ordenes;
        this.tecnicos = tecnicos;
    }

    public OrdenTrabajo crear(Long supervisorId, Crear datos, String authorization) {
        String cliente = datos.cliente().strip();
        String direccion = datos.direccion().strip();
        String descripcion = datos.descripcion().strip();
        var anterior = ordenes.findById(datos.id());
        if (anterior.isPresent()) {
            OrdenTrabajo orden = anterior.get();
            if (supervisorId.equals(orden.getSupervisorId()) && datos.tecnicoId().equals(orden.getTecnicoId())
                    && cliente.equals(orden.getCliente()) && direccion.equals(orden.getDireccion())
                    && descripcion.equals(orden.getDescripcion()) && datos.fecha().equals(orden.getFecha())) {
                return orden;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya está asociada a otra orden");
        }

        tecnicos.validarAsignacion(datos.tecnicoId(), authorization);
        return ordenes.save(new OrdenTrabajo(
                datos.id(), supervisorId, datos.tecnicoId(), cliente, direccion, descripcion, datos.fecha()));
    }

    public List<OrdenTrabajo> listar(Long tecnicoId) {
        return ordenes.findByTecnicoIdOrderByFechaDescIdAsc(tecnicoId);
    }

    @Transactional
    public OrdenTrabajo cambiarEstado(Long tecnicoId, UUID id, OrdenTrabajo.Estado estado) {
        OrdenTrabajo orden = ordenes.findByIdAndTecnicoId(id, tecnicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        orden.cambiarEstado(estado);
        return orden;
    }
}
