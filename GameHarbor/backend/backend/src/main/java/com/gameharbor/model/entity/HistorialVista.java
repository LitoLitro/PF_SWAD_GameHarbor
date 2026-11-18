package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Registro de interacciones del usuario con un juego. Es una de las fuentes de datos del modelo de ML. */
@Entity
@Table(name = "HISTORIAL_VISTA")
public class HistorialVista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORIAL_VISTA_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @Column(name = "HISTORIAL_FECHA", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "HISTORIAL_SEGUNDOS", nullable = false)
    private int segundos;

    @Column(name = "HISTORIAL_ORIGEN", nullable = false, length = 20)
    private String origen = "CATALOGO";

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
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public int getSegundos() { return segundos; }
    public void setSegundos(int segundos) { this.segundos = segundos; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}
