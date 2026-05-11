package com.alvaroorgaz.easyfactura.security;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class EmpresaLoginDetails implements UserDetails {
    private final Empresa empresa;

    public EmpresaLoginDetails(Empresa empresa) {
        this.empresa = empresa;
    }

    public Empresa getEmpresa() {
        return empresa;
    }




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (empresa.getRol() == Rol.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_EMPRESA"));
    }

    @Override
    public String getPassword() {
        return empresa.getPassword();
    }

    @Override
    public String getUsername() {
        return empresa.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
