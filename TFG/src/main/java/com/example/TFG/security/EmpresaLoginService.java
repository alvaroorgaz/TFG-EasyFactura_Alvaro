package com.example.TFG.security;

import com.example.TFG.model.Empresa;
import com.example.TFG.repository.EmpresaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
@Service
public class EmpresaLoginService implements UserDetailsService {

    public final EmpresaRepository empresaRepository;

    public EmpresaLoginService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Empresa empresa = empresaRepository.findAll().stream()
                .filter(e -> e.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Empresa no encontrada con email: " + email));
        return new EmpresaLoginDetails(empresa);


    }
}
