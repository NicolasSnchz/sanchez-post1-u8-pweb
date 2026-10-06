package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.repository.CategoriaRepository;
import com.universidad.catalogo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;

    public ProductoService(ProductoRepository productoRepo, CategoriaRepository categoriaRepo) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
    }

    public List<Producto> listarTodos() {
        return productoRepo.findAllConCategoria();
    }

    public Producto buscarPorId(Long id) {
        return productoRepo.findByIdConCategoria(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }

    @Transactional
    public Producto guardar(Producto datos, Long categoriaId) {
        Categoria categoria = categoriaRepo.findById(categoriaId)
            .orElseThrow(() -> new RuntimeException("Categoria no encontrada: " + categoriaId));

        // En edicion se actualiza la entidad administrada; en creacion se usa la nueva
        Producto producto = datos;
        if (datos.getId() != null) {
            producto = productoRepo.findById(datos.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + datos.getId()));
            producto.setNombre(datos.getNombre());
            producto.setPrecio(datos.getPrecio());
            producto.setStock(datos.getStock());
        }
        producto.asignarCategoria(categoria);
        return productoRepo.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepo.deleteById(id);
    }

    public List<Producto> listarPorCategoriaConPrecioMayorA(Long categoriaId, BigDecimal precioMinimo) {
        return productoRepo.buscarPorCategoriaConPrecioMayorA(categoriaId, precioMinimo);
    }
}
