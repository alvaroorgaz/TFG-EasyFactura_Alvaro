package com.example.TFG.service;

import com.example.TFG.model.Producto;
import com.example.TFG.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    public Producto obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }
}
