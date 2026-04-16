package com.example.TFG.controller;

import com.example.TFG.model.Empresa;
import com.example.TFG.model.Producto;
import com.example.TFG.service.EmpresaService;
import com.example.TFG.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;
    private final EmpresaService empresaService;

    public ProductoController(ProductoService productoService, EmpresaService empresaService) {
        this.productoService = productoService;
        this.empresaService = empresaService;
    }

    @GetMapping
    public String listaProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerProductos());
        return "producto/lista";
    }

    @GetMapping("/crear")
    public String crearProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("empresas", empresaService.obtenerEmpresas());
        return "producto/crear";
    }

    @PostMapping
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam Long empresaId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = empresaService.obtenerEmpresaPorId(empresaId);

            if (empresa == null) {
                model.addAttribute("producto", producto);
                model.addAttribute("empresas", empresaService.obtenerEmpresas());
                model.addAttribute("error", "La empresa seleccionada no existe.");
                return "producto/crear";
            }

            boolean nuevo = (producto.getIdProducto() == null);

            producto.setEmpresa(empresa);
            productoService.guardarProducto(producto);

            if (nuevo) {
                redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Producto actualizado exitosamente.");
            }

            return "redirect:/producto";

        } catch (Exception e) {
            model.addAttribute("producto", producto);
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
            model.addAttribute("error", "Error al guardar el producto: " + e.getMessage());
            return "producto/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarProductoForm(@PathVariable Integer id,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Producto producto = productoService.obtenerProductoPorId(id);

        if (producto == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/producto";
        }

        model.addAttribute("producto", producto);
        model.addAttribute("empresas", empresaService.obtenerEmpresas());
        return "producto/crear";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id,
                                   RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el producto: " + e.getMessage());
        }

        return "redirect:/producto";
    }
}
