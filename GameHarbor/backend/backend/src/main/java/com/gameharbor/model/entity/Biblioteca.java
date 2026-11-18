package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BIBLIOTECA")
public class Biblioteca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BIBLIOTECA_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @Column(name = "BIBLIOTECA_FECHA_ADQUISICION", nullable = false)
    private LocalDateTime fechaAdquisicion;

    @PrePersist
    void prePersist() {
        if (fechaAdquisicion == null) fechaAdquisicion = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
    public LocalDateTime getFechaAdquisicion() { return fechaAdquisicion; }
    public void setFechaAdquisicion(LocalDateTime fechaAdquisicion) { this.fechaAdquisicion = fechaAdquisicion; }
}
