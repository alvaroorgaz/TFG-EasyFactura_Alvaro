package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.Producto;
import com.alvaroorgaz.easyfactura.service.AuthService;
import com.alvaroorgaz.easyfactura.service.EmpresaService;
import com.alvaroorgaz.easyfactura.service.ProductoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;
    private final EmpresaService empresaService;
    private final AuthService authService;

    public ProductoController(ProductoService productoService,
                              EmpresaService empresaService,
                              AuthService authService) {
        this.productoService = productoService;
        this.empresaService = empresaService;
        this.authService = authService;
    }

    @GetMapping
    public String listaProductos(Model model, Authentication authentication) {
        Empresa empresaLogueada = authService.getEmpresaLogin();
        boolean esAdmin = authService.isAdmin();

        if (esAdmin) {
            model.addAttribute("productos", productoService.obtenerProductos());
        } else {
            model.addAttribute("productos", productoService.obtenerProductosPorEmpresa(empresaLogueada.getId_empresa()));
        }

        model.addAttribute("esAdmin", esAdmin);
        return "producto/lista";
    }

    @GetMapping("/crear")
    public String crearProductoForm(Model model, Authentication authentication) {
        boolean esAdmin = authService.isAdmin();

        model.addAttribute("producto", new Producto());
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
        }

        return "producto/crear";
    }

    @PostMapping
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam(required = false) Long empresaId,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            boolean esAdmin = authService.isAdmin();
            Empresa empresa;

            if (esAdmin) {
                empresa = empresaService.obtenerEmpresaPorId(empresaId);
            } else {
                empresa = authService.getEmpresaLogin();
            }

            if (empresa == null) {
                model.addAttribute("producto", producto);
                model.addAttribute("esAdmin", esAdmin);
                if (esAdmin) {
                    model.addAttribute("empresas", empresaService.obtenerEmpresas());
                }
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
            boolean esAdmin = authService.isAdmin();
            model.addAttribute("producto", producto);
            model.addAttribute("esAdmin", esAdmin);
            if (esAdmin) {
                model.addAttribute("empresas", empresaService.obtenerEmpresas());
            }
            model.addAttribute("error", "Error al guardar el producto: " + e.getMessage());
            return "producto/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarProductoForm(@PathVariable Integer id,
                                     Authentication authentication,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Producto producto = productoService.obtenerProductoPorId(id);

        if (producto == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/producto";
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

        if (!esAdmin && !producto.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este producto.");
            return "redirect:/producto";
        }

        model.addAttribute("producto", producto);
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
        }

        return "producto/crear";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        Producto producto = productoService.obtenerProductoPorId(id);

        if (producto == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/producto";
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

        if (!esAdmin && !producto.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este producto.");
            return "redirect:/producto";
        }

        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el producto: " + e.getMessage());
        }

        return "redirect:/producto";
    }
}
