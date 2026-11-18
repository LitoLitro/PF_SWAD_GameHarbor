package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Sesion de cloud gaming (simulada): un usuario jugando un juego de su biblioteca en un servidor. */
@Entity
@Table(name = "SESION_JUEGO")
public class SesionJuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SESION_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SERVIDOR_CLOUD_ID")
    private ServidorCloud servidor;

    @Column(name = "SESION_FECHA_INICIO", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "SESION_FECHA_FIN")
    private LocalDateTime fechaFin;

    @Column(name = "SESION_ESTADO", nullable = false, length = 20)
    private String estado = "ACTIVA";

    @Column(name = "SESION_BITRATE_MBPS", precision = 6, scale = 2)
    private BigDecimal bitrateMbps;

    @Column(name = "SESION_LATENCIA_MS")
    private Integer latenciaMs;

    @PrePersist
    void prePersist() {
        if (fechaInicio == null) fechaInicio = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
    public ServidorCloud getServidor() { return servidor; }
    public void setServidor(ServidorCloud servidor) { this.servidor = servidor; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getBitrateMbps() { return bitrateMbps; }
    public void setBitrateMbps(BigDecimal bitrateMbps) { this.bitrateMbps = bitrateMbps; }
    public Integer getLatenciaMs() { return latenciaMs; }
    public void setLatenciaMs(Integer latenciaMs) { this.latenciaMs = latenciaMs; }
}
