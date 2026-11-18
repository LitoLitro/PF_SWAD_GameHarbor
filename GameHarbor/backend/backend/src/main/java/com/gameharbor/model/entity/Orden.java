package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDEN")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDEN_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "METODO_PAGO_ID")
    private MetodoPago metodoPago;

    @Column(name = "ORDEN_FECHA", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ORDEN_TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "ORDEN_ESTADO", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDetalle> detalles = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
    }

    public void agregarDetalle(OrdenDetalle detalle) {
        detalle.setOrden(this);
        detalles.add(detalle);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<OrdenDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<OrdenDetalle> detalles) { this.detalles = detalles; }
}
