package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.Rol;
import com.alvaroorgaz.easyfactura.repository.EmpresaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final CertificadoEmpresaService certificadoEmpresaService;

    public EmpresaService(EmpresaRepository empresaRepository,
                          PasswordEncoder passwordEncoder,
                          CertificadoEmpresaService certificadoEmpresaService) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.certificadoEmpresaService = certificadoEmpresaService;
    }

    public Empresa crearEmpresa(Empresa empresa) {
        if (empresa.getId_empresa() == null) {
            if (empresa.getRol() == null) {
                empresa.setRol(Rol.EMPRESA);
            }
            empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
            Empresa empresaGuardada = empresaRepository.save(empresa);
            certificadoEmpresaService.generarCertificadoSiNoExiste(empresaGuardada);
            return empresaGuardada;
        }

        Empresa empresaExistente = empresaRepository.findById(empresa.getId_empresa()).orElse(null);

        if (empresaExistente != null) {
            if (empresa.getPassword() == null || empresa.getPassword().isBlank()) {
                empresa.setPassword(empresaExistente.getPassword());
            } else {
                empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
            }

            if (empresa.getRol() == null) {
                empresa.setRol(empresaExistente.getRol() != null ? empresaExistente.getRol() : Rol.EMPRESA);
            }
        }

        Empresa empresaGuardada = empresaRepository.save(empresa);
        certificadoEmpresaService.generarCertificadoSiNoExiste(empresaGuardada);
        return empresaGuardada;
    }

    public List<Empresa> obtenerEmpresas() {
        return empresaRepository.findAll();
    }

    public Empresa obtenerEmpresaPorId(Integer id) {
        return empresaRepository.findById(id).orElse(null);
    }

    public Empresa obtenerEmpresaPorEmail(String email) {
        return empresaRepository.findByEmail(email).orElse(null);
    }

    public void eliminarEmpresa(Integer id) {
        empresaRepository.deleteById(id);
    }
}
