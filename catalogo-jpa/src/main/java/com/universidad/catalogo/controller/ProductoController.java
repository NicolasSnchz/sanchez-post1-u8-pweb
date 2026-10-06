package com.universidad.catalogo.controller;

import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.service.CategoriaService;
import com.universidad.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarNuevo(Model model) {
        return prepararFormulario(new Producto(), null, "Nuevo Producto", model);
    }

    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        return prepararFormulario(producto, producto.getCategoria().getId(), "Editar Producto", model);
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Producto producto,
                          BindingResult result,
                          @RequestParam(required = false) Long categoriaId,
                          Model model) {
        if (categoriaId == null) {
            // Sin categoria no se llega al servicio: evita productos sin categoria valida
            model.addAttribute("errorCategoria", "Debe seleccionar una categoria");
        }
        if (result.hasErrors() || categoriaId == null) {
            String titulo = producto.getId() == null ? "Nuevo Producto" : "Editar Producto";
            return prepararFormulario(producto, categoriaId, titulo, model);
        }
        productoService.guardar(producto, categoriaId);
        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/productos";
    }

    // Endpoint que usa la consulta JPQL personalizada del repositorio
    @GetMapping("/categoria/{categoriaId}/precio-mayor")
    public String porCategoriaConPrecioMayor(@PathVariable Long categoriaId,
                                             @RequestParam BigDecimal minimo,
                                             Model model) {
        model.addAttribute("productos",
            productoService.listarPorCategoriaConPrecioMayorA(categoriaId, minimo));
        model.addAttribute("categoria", categoriaService.buscarPorId(categoriaId));
        model.addAttribute("minimo", minimo);
        return "productos/filtrados";
    }

    private String prepararFormulario(Producto producto, Long categoriaId, String titulo, Model model) {
        model.addAttribute("producto", producto);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("titulo", titulo);
        return "productos/formulario";
    }
}
