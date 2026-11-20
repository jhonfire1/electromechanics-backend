package cl.electromechanics.operations.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
@Table(name = "ordenes_trabajo")
public class OrdenTrabajo {
    @Id
    private UUID id;
    @Version
    private Long version;
    @Column(nullable = false)
    private Long supervisorId;
    @Column(nullable = false)
    private Long tecnicoId;
    @Column(nullable = false, length = 120)
    private String cliente;
    @Column(nullable = false, length = 200)
    private String direccion;
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Column(nullable = false)
    private LocalDate fecha;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    public enum Estado {
        PENDIENTE, EN_PROCESO, COMPLETADA
    }

    protected OrdenTrabajo() {}

    public OrdenTrabajo(UUID id, Long supervisorId, Long tecnicoId, String cliente,
                        String direccion, String descripcion, LocalDate fecha) {
        this.id = id;
        this.supervisorId = supervisorId;
        this.tecnicoId = tecnicoId;
        this.cliente = cliente;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estado = Estado.PENDIENTE;
    }

    public void cambiarEstado(Estado nuevoEstado) {
        if (estado == nuevoEstado) {
            return;
        }
        if ((estado == Estado.PENDIENTE && nuevoEstado == Estado.EN_PROCESO)
                || (estado == Estado.EN_PROCESO && nuevoEstado == Estado.COMPLETADA)) {
            estado = nuevoEstado;
            return;
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Cambio de estado no permitido");
    }

    public UUID getId() { return id; }
    public Long getSupervisorId() { return supervisorId; }
    public Long getTecnicoId() { return tecnicoId; }
    public String getCliente() { return cliente; }
    public String getDireccion() { return direccion; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public Estado getEstado() { return estado; }
}
