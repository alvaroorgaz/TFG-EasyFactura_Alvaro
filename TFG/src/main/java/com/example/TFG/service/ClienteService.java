package com.example.TFG.service;

import com.example.TFG.model.Cliente;
import com.example.TFG.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente guardarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    public List<Cliente> obtenerClientesPorEmpresa(Long idEmpresa) {
        return clienteRepository.findClientesByEmpresaId(idEmpresa);
    }

    public Cliente obtenerClientePorId(Integer id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public void eliminarCliente(Integer id) {
        clienteRepository.deleteById(id);
    }
}
