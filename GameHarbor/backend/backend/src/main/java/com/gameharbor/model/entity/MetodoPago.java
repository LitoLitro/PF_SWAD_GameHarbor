package com.gameharbor.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "METODO_PAGO")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "METODO_PAGO_ID")
    private Integer id;

    @Column(name = "METODO_PAGO_NOMBRE", nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "METODO_PAGO_ACTIVO", nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
