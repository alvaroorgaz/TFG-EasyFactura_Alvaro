package com.example.TFG.service;

import com.example.TFG.model.Empresa;
import com.example.TFG.repository.EmpresaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpresaService(EmpresaRepository empresaRepository, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Empresa crearEmpresa(Empresa empresa) {
        if (empresa.getId_empresa() == null) {
            empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
            return empresaRepository.save(empresa);
        }

        Empresa empresaExistente = empresaRepository.findById(empresa.getId_empresa()).orElse(null);

        if (empresaExistente != null) {
            if (empresa.getPassword() == null || empresa.getPassword().isBlank()) {
                empresa.setPassword(empresaExistente.getPassword());
            } else {
                empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
            }
        }

        return empresaRepository.save(empresa);
    }

    public List<Empresa> obtenerEmpresas() {
        return empresaRepository.findAll();
    }

    public Empresa obtenerEmpresaPorId(Long id) {
        return empresaRepository.findById(id).orElse(null);
    }

    public void eliminarEmpresa(Long id) {
        empresaRepository.deleteById(id);
    }
}
