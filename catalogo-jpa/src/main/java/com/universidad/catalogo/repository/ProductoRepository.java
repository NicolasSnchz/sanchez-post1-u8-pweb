package com.universidad.catalogo.repository;

import com.universidad.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // JOIN FETCH para cargar la categoria en la misma consulta (evita N+1 al
    // listar productos, ya que Producto.categoria se declaro LAZY explicitamente)
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria ORDER BY p.nombre")
    List<Producto> findAllConCategoria();

    // Carga un producto con su categoria para el formulario de edicion
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.id = :id")
    Optional<Producto> findByIdConCategoria(@Param("id") Long id);

    // Consulta JPQL personalizada que cruza Producto y Categoria:
    // productos de una categoria especifica con precio mayor a un umbral.
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c " +
           "WHERE c.id = :categoriaId AND p.precio > :precioMinimo " +
           "ORDER BY p.precio DESC")
    List<Producto> buscarPorCategoriaConPrecioMayorA(
        @Param("categoriaId") Long categoriaId,
        @Param("precioMinimo") BigDecimal precioMinimo);
}
