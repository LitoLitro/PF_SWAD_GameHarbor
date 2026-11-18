package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "RESENA")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESENA_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @Column(name = "RESENA_CALIFICACION", nullable = false)
    private int calificacion;

    @Column(name = "RESENA_COMENTARIO", length = 500)
    private String comentario;

    @Column(name = "RESENA_FECHA", nullable = false)
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
    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
