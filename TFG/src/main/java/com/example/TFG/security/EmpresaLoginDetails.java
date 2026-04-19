package com.example.TFG.security;

import com.example.TFG.model.Empresa;
import org.jspecify.annotations.Nullable;
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
        if (empresa.getEmail().equals("orgazmonleonalvaro@gmail.com") && empresa.getNombre().equals("ADMIN") && empresa.getCif().equals("ADMIN")) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_EMPRESA"));
    }

    @Override
    public @Nullable String getPassword() {
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
