package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USUARIO_ID")
    private Integer id;

    @Column(name = "USUARIO_NOMBRE", nullable = false, length = 80)
    private String nombre;

    @Column(name = "USUARIO_EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "USUARIO_PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "USUARIO_SALDO", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "USUARIO_ESTADO", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "USUARIO_FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USUARIO_ROL",
            joinColumns = @JoinColumn(name = "USUARIO_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROL_ID"))
    private Set<Rol> roles = new HashSet<>();

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}
