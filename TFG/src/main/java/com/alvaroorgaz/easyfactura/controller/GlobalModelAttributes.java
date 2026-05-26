package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final AuthService authService;

    public GlobalModelAttributes(AuthService authService) {
        this.authService = authService;
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        return authService.isAdmin();
    }

    @ModelAttribute("empresaLoginNombre")
    public String empresaLoginNombre() {
        Empresa empresa = authService.getEmpresaLogin();
        return empresa != null ? empresa.getNombre() : null;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
