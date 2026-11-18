package com.gameharbor.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CARRITO_ITEM")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARRITO_ITEM_ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CARRITO_ID")
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JUEGO_ID")
    private Juego juego;

    @Column(name = "CARRITO_ITEM_CANTIDAD", nullable = false)
    private int cantidad = 1;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Carrito getCarrito() { return carrito; }
    public void setCarrito(Carrito carrito) { this.carrito = carrito; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
