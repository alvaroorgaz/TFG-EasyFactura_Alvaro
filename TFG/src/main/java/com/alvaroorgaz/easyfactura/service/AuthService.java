package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.Rol;
import com.alvaroorgaz.easyfactura.security.EmpresaLoginDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

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
}
