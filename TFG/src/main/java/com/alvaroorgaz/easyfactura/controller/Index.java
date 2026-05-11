package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.service.ClienteService;
import com.alvaroorgaz.easyfactura.service.EmpresaService;
import com.alvaroorgaz.easyfactura.service.FacturaService;
import com.alvaroorgaz.easyfactura.service.ProductoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Index {

    private final EmpresaService empresaService;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final FacturaService facturaService;

    public Index(EmpresaService empresaService,
                 ClienteService clienteService,
                 ProductoService productoService,
                 FacturaService facturaService) {
        this.empresaService = empresaService;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.facturaService = facturaService;
    }

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        boolean esAdmin = false;

        if (authentication != null && authentication.getAuthorities() != null) {
            esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }

        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        int totalClientes;
        int totalProductos;
        int totalFacturas;

        if (esAdmin) {
            totalClientes = clienteService.obtenerClientes().size();
            totalProductos = productoService.obtenerProductos().size();
            totalFacturas = facturaService.obtenerFacturas(true, null).size();
        } else {
            totalClientes = clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()).size();
            totalProductos = productoService.obtenerProductosPorEmpresa(empresaLogueada.getId_empresa()).size();
            totalFacturas = facturaService.obtenerFacturas(false, empresaLogueada).size();
        }

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalFacturas", totalFacturas);
        return "index";
    }

    private Empresa obtenerEmpresaLogueada(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return empresaService.obtenerEmpresas().stream()
                .filter(e -> e.getEmail().equals(authentication.getName()))
                .findFirst()
                .orElse(null);
    }
}
