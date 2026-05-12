package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.Factura;
import com.alvaroorgaz.easyfactura.model.Producto;
import com.alvaroorgaz.easyfactura.model.Rol;
import com.alvaroorgaz.easyfactura.repository.ClienteRepository;
import com.alvaroorgaz.easyfactura.repository.EmpresaRepository;
import com.alvaroorgaz.easyfactura.repository.FacturaRepository;
import com.alvaroorgaz.easyfactura.repository.ProductoRepository;
import com.alvaroorgaz.easyfactura.security.EmpresaLoginDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final FacturaRepository facturaRepository;

    public AuthService(EmpresaRepository empresaRepository,
                       ClienteRepository clienteRepository,
                       ProductoRepository productoRepository,
                       FacturaRepository facturaRepository) {
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.facturaRepository = facturaRepository;
    }

    public Empresa getEmpresaLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof EmpresaLoginDetails empresaLoginDetails)) {
            return null;
        }

        return empresaLoginDetails.getEmpresa();
    }

    public boolean isAdmin() {
        Empresa empresa = getEmpresaLogin();
        return empresa != null && empresa.getRol() == Rol.ADMIN;
    }

    public String getEmailLogin() {
        Empresa empresa = getEmpresaLogin();
        return empresa != null ? empresa.getEmail() : null;
    }

    public boolean esMismaEmpresa(Integer empresaId) {
        Empresa empresa = getEmpresaLogin();
        return empresa != null && empresaId != null && empresaId.equals(empresa.getId_empresa());
    }

    public boolean esPropietarioCliente(Integer id) {
        Empresa empresa = getEmpresaLogin();
        if (empresa == null || id == null) {
            return false;
        }

        return clienteRepository.findById(id)
                .map(cliente -> cliente.getEmpresa().getId_empresa().equals(empresa.getId_empresa()))
                .orElse(false);
    }

    public boolean esPropietarioProducto(Integer id) {
        Empresa empresa = getEmpresaLogin();
        if (empresa == null || id == null) {
            return false;
        }

        return productoRepository.findById(id)
                .map(producto -> producto.getEmpresa().getId_empresa().equals(empresa.getId_empresa()))
                .orElse(false);
    }

    public boolean esPropietarioFactura(Integer id) {
        Empresa empresa = getEmpresaLogin();
        if (empresa == null || id == null) {
            return false;
        }

        return facturaRepository.findById(id)
                .map(factura -> factura.getEmpresa().getId_empresa().equals(empresa.getId_empresa()))
                .orElse(false);
    }

    public boolean esAdminPorId(Integer id) {
        return empresaRepository.findById(id)
                .map(Empresa::getRol)
                .map(rol -> rol == Rol.ADMIN)
                .orElse(false);
    }
}
