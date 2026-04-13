package com.example.TFG.service;

import com.example.TFG.model.Empresa;
import com.example.TFG.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;
    public EmpresaService (EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Empresa crearEmpresa(Empresa empresa) {
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
