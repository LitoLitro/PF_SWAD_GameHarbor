package com.gameharbor.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DESARROLLADOR")
public class Desarrollador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DESARROLLADOR_ID")
    private Integer id;

    @Column(name = "DESARROLLADOR_NOMBRE", nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(name = "DESARROLLADOR_PAIS", length = 60)
    private String pais;

    @Column(name = "DESARROLLADOR_SITIO_WEB", length = 200)
    private String sitioWeb;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getSitioWeb() { return sitioWeb; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }
}
