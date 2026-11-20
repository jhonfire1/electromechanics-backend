package cl.electromechanics.operations.controller;

import cl.electromechanics.operations.dto.OrdenDtos.*;
import cl.electromechanics.operations.security.AccesoOrdenes;
import cl.electromechanics.operations.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenTrabajoController {
    private final OrdenTrabajoService ordenes;
    private final AccesoOrdenes acceso;

    public OrdenTrabajoController(OrdenTrabajoService ordenes, AccesoOrdenes acceso) {
        this.ordenes = ordenes;
        this.acceso = acceso;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Respuesta crear(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                           @Valid @RequestBody Crear datos) {
        Long supervisorId = acceso.exigirRol(token, "SUPERVISOR");
        return Respuesta.desde(ordenes.crear(supervisorId, datos, token));
    }

    @GetMapping("/asignadas")
    public List<Respuesta> asignadas(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long tecnicoId = acceso.exigirRol(token, "TECNICO");
        return ordenes.listar(tecnicoId).stream().map(Respuesta::desde).toList();
    }

    @PutMapping("/{id}/estado")
    public Respuesta cambiarEstado(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable UUID id, @Valid @RequestBody CambioEstado cambio) {
        Long tecnicoId = acceso.exigirRol(token, "TECNICO");
        return Respuesta.desde(ordenes.cambiarEstado(tecnicoId, id, cambio.estado()));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<Void> conflicto() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
