package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ORDEN_DETALLE")
public class OrdenDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDEN_DETALLE_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDEN_ID")
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @Column(name = "ORDEN_DETALLE_PRECIO_UNITARIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "ORDEN_DETALLE_CANTIDAD", nullable = false)
    private int cantidad = 1;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
