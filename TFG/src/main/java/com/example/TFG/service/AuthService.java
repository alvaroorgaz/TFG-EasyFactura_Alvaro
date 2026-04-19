package com.example.TFG.service;

import com.example.TFG.model.Empresa;
import com.example.TFG.security.EmpresaLoginDetails;
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
