package com.alvaroorgaz.easyfactura.config;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.Rol;
import com.alvaroorgaz.easyfactura.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.nombre}")
    private String adminNombre;

    @Value("${app.admin.cif}")
    private String adminCif;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminInitializer(EmpresaRepository empresaRepository, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (empresaRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        Empresa admin = new Empresa();
        admin.setNombre(adminNombre);
        admin.setCif(adminCif);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setDireccion("Administrador del sistema");
        admin.setTelefono("000000000");
        admin.setRol(Rol.ADMIN);

        empresaRepository.save(admin);
    }
}
