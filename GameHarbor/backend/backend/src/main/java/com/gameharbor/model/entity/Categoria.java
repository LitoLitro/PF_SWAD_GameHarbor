package com.gameharbor.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORIA_ID")
    private Integer id;

    @Column(name = "CATEGORIA_NOMBRE", nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "CATEGORIA_DESCRIPCION", length = 300)
    private String descripcion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
