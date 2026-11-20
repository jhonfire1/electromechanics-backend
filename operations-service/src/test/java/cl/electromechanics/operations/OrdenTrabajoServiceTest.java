package cl.electromechanics.operations;

import cl.electromechanics.operations.dto.OrdenDtos.Crear;
import cl.electromechanics.operations.entity.OrdenTrabajo;
import cl.electromechanics.operations.entity.OrdenTrabajo.Estado;
import cl.electromechanics.operations.repository.OrdenTrabajoRepository;
import cl.electromechanics.operations.service.OrdenTrabajoService;
import cl.electromechanics.operations.service.TecnicosClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrdenTrabajoServiceTest {
    private final OrdenTrabajoRepository repository = mock(OrdenTrabajoRepository.class);
    private final TecnicosClient tecnicos = mock(TecnicosClient.class);
    private final OrdenTrabajoService service = new OrdenTrabajoService(repository, tecnicos);
    private final Crear solicitud = new Crear(
            UUID.randomUUID(), 7L, "  Taller  ", "Av. Central 10", "Revisar motor", LocalDate.of(2026, 11, 25));

    @Test
    void validaAlTecnicoYGuardaLaOrdenPendiente() {
        when(repository.save(any(OrdenTrabajo.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var orden = service.crear(3L, solicitud, "Bearer prueba");

        verify(tecnicos).validarAsignacion(7L, "Bearer prueba");
        assertEquals(7L, orden.getTecnicoId());
        assertEquals("Taller", orden.getCliente());
        assertEquals(Estado.PENDIENTE, orden.getEstado());
    }

    @Test
    void unReintentoIgualDevuelveLaMismaOrden() {
        OrdenTrabajo orden = ordenExistente();
        when(repository.findById(solicitud.id())).thenReturn(Optional.of(orden));

        assertSame(orden, service.crear(3L, solicitud, "Bearer prueba"));
        verifyNoInteractions(tecnicos);
        verify(repository, never()).save(any());
    }

    @Test
    void tecnicoDistintoNoPuedeModificarLaOrden() {
        when(repository.findByIdAndTecnicoId(solicitud.id(), 8L)).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(8L, solicitud.id(), Estado.EN_PROCESO));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void cambioDeEstadoDebeSeguirSuSecuencia() {
        OrdenTrabajo orden = ordenExistente();
        when(repository.findByIdAndTecnicoId(solicitud.id(), 7L)).thenReturn(Optional.of(orden));

        var error = assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(7L, solicitud.id(), Estado.COMPLETADA));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(Estado.EN_PROCESO, service.cambiarEstado(7L, solicitud.id(), Estado.EN_PROCESO).getEstado());
        assertEquals(Estado.COMPLETADA, service.cambiarEstado(7L, solicitud.id(), Estado.COMPLETADA).getEstado());
    }

    private OrdenTrabajo ordenExistente() {
        return new OrdenTrabajo(solicitud.id(), 3L, 7L, "Taller", solicitud.direccion(),
                solicitud.descripcion(), solicitud.fecha());
    }
}
