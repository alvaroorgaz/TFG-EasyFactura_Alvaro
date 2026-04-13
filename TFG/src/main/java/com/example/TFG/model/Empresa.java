package com.example.TFG.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmpresa;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String cif;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 30)
    private String password;

    private String direccion;

    private Integer telefono;

    @OneToMany(mappedBy = "empresa")
    private List<Cliente> clientes;

    @OneToMany(mappedBy = "empresa")
    private List<Producto> productos;

    @OneToMany(mappedBy = "empresa")
    private List<Factura> facturas;

    // Getters y setters
}
