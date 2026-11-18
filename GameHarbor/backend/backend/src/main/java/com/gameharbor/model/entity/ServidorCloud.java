package com.gameharbor.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVIDOR_CLOUD")
public class ServidorCloud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVIDOR_CLOUD_ID")
    private Integer id;

    @Column(name = "SERVIDOR_CLOUD_NOMBRE", nullable = false, length = 80)
    private String nombre;

    @Column(name = "SERVIDOR_CLOUD_UBICACION", length = 80)
    private String ubicacion;

    @Column(name = "SERVIDOR_CLOUD_IP", nullable = false, unique = true, length = 45)
    private String ip;

    @Column(name = "SERVIDOR_CLOUD_CAPACIDAD", nullable = false)
    private int capacidad;

    @Column(name = "SERVIDOR_CLOUD_ESTADO", nullable = false, length = 20)
    private String estado = "ACTIVO";

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
