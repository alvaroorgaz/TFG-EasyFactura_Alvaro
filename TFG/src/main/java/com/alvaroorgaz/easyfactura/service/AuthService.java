package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.security.EmpresaLoginDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthService {
    public Empresa getEmpresaLogin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EmpresaLoginDetails empresaLoginDetails = (EmpresaLoginDetails) auth.getPrincipal();
        return empresaLoginDetails.getEmpresa();

    }
    public boolean isAdmin(){
        return getEmpresaLogin().equals("orgazmonleonalvaro@gmail.com");

    }

}
