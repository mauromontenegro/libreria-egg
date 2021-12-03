package com.egg.libreriaEgg.repositorios;

import com.egg.libreriaEgg.entidades.Libro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * (LibroRepositorio) debe contener los métodos necesarios para
 * guardar/actualizar libros en la base de datos, realizar consultas o dar de
 * baja según corresponda. Extiende de JpaRepository: será un repositorio de
 * Libro con la Primary Key de tipo String.
 *
 * Los métodos save(), findById() y delete() se implementan por JpaRepository.
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Repository
public interface LibroRepositorio extends JpaRepository<Libro, String> {

    // Método que devuelve el Libro vinculado a un ISBN:
    @Query("SELECT lib FROM Libro lib WHERE lib.isbn = :isbn")
    public Libro buscarPorIsbn(@Param("isbn") Long isbn);

    // Método que devuelve el/los Libro/s vinculado a un Autor:
    @Query("SELECT lib FROM Libro lib WHERE lib.autor.id = :id")
    public List<Libro> buscarPorAutor(@Param("id") String id);

    // Método que devuelve el/los Libro/s vinculado a una Editorial:
    @Query("SELECT lib FROM Libro lib WHERE lib.editorial.id = :id")
    public List<Libro> buscarPorEditorial(@Param("id") String id);
    
    // Método que sólo devuelve los libros dados de alta.
    @Override
    @Query("SELECT lib FROM Libro lib WHERE lib.alta IS true ORDER BY lib.titulo ASC")
    public List<Libro> findAll();
    
    // Método que sólo devuelve los libros dados de baja.
    @Query("SELECT lib FROM Libro lib WHERE lib.alta IS false ORDER BY lib.titulo ASC")
    public List<Libro> listarDeBaja();
    
}