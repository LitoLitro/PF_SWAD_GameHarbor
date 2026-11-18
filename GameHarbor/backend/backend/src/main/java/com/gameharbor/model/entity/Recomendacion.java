package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resultado del modelo de ML (arbol de decision) para un par usuario-juego. */
@Entity
@Table(name = "RECOMENDACION")
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECOMENDACION_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @Column(name = "RECOMENDACION_PUNTAJE", nullable = false, precision = 5, scale = 4)
    private BigDecimal puntaje;

    @Column(name = "RECOMENDACION_ORIGEN", nullable = false, length = 20)
    private String origen = "ML";

    @Column(name = "RECOMENDACION_FECHA", nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
    public BigDecimal getPuntaje() { return puntaje; }
    public void setPuntaje(BigDecimal puntaje) { this.puntaje = puntaje; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
