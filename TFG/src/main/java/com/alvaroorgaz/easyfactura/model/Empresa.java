package com.alvaroorgaz.easyfactura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_empresa;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El CIF es obligatorio.")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "El CIF debe tener un formato valido.")
    @Column(nullable = false, unique = true, length = 20)
    private String cif;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email no tiene un formato valido.")
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 72)
    private String password;

    private String direccion;

    @Pattern(regexp = "^[0-9]{9,20}$", message = "El telefono debe contener entre 9 y 20 digitos.")
    @Column(length = 20)
    private String telefono;

    @OneToMany(mappedBy = "empresa")
    private List<Cliente> clientes;

    @OneToMany(mappedBy = "empresa")
    private List<Producto> productos;

    @OneToMany(mappedBy = "empresa")
    private List<Factura> facturas;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    public Long getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(Long idEmpresa) {
        this.id_empresa = idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public List<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(List<Factura> facturas) {
        this.facturas = facturas;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
