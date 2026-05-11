package com.alvaroorgaz.easyfactura.security;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.repository.EmpresaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmpresaLoginService implements UserDetailsService {

    public final EmpresaRepository empresaRepository;

    public EmpresaLoginService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Empresa no encontrada con email: " + email));
        return new EmpresaLoginDetails(empresa);
    }
}
