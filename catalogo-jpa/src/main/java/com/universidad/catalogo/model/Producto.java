package com.universidad.catalogo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio admite maximo 8 enteros y 2 decimales")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock")
    private int stock;

    // Lado propietario de la relacion: guarda la clave foranea categoria_id.
    // fetch = LAZY se declara explicitamente porque el valor por defecto de @ManyToOne es EAGER.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false,
                foreignKey = @ForeignKey(name = "FK_categoria"))
    private Categoria categoria;

    // Constructor vacio requerido por JPA
    public Producto() {}

    /**
     * Metodo helper del lado propietario: asigna la categoria y mantiene
     * sincronizada la lista inversa Categoria.productos en memoria.
     */
    public void asignarCategoria(Categoria nuevaCategoria) {
        if (this.categoria != null && this.categoria != nuevaCategoria) {
            this.categoria.getProductos().remove(this);
        }
        this.categoria = nuevaCategoria;
        if (nuevaCategoria != null && !nuevaCategoria.getProductos().contains(this)) {
            nuevaCategoria.getProductos().add(this);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public Categoria getCategoria() { return categoria; }
}
