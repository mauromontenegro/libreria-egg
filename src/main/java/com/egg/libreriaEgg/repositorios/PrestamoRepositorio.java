package com.egg.libreriaEgg.repositorios;

import com.egg.libreriaEgg.entidades.Prestamo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * (PrestamoRepositorio) debe contener los métodos necesarios para registrar un
 * préstamo en la base de datos, realizar consultas y realizar devoluciones,
 * etc.
 *
 * Los métodos save(), findById() y delete() se implementan por JpaRepository.
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Repository
public interface PrestamoRepositorio extends JpaRepository<Prestamo, String> {

    // Método que devuelve el/los Prestamo/s vinculado a un Libro:
    @Query("SELECT p FROM Prestamo p WHERE p.libro.id = :idLibro")
    public List<Prestamo> buscarPorLibro(@Param("idLibro") String idLibro);

    // Método que devuelve el/los Prestamo/s vinculado a un Usuario:
    @Query("SELECT p FROM Prestamo p WHERE p.usuario.id = :idUsuario")
    public List<Prestamo> buscarPorUsuario(@Param("idUsuario") String idUsuario);

    // Método que devuelve el/los Prestamo/s dados de Alta:
    @Query("SELECT p FROM Prestamo p WHERE p.alta = true")
    public List<Prestamo> buscarPrestamosAlta();

    // Método que devuelve el/los Prestamo/s dados de Baja:
    @Query("SELECT p FROM Prestamo p WHERE p.alta = false")
    public List<Prestamo> buscarPrestamosBaja();

    // Método que devuelve el/los Prestamo/s dados de Alta de un Usuario:
    @Query("SELECT p FROM Prestamo p WHERE p.alta = true AND p.usuario.id = :idUsuario")
    public List<Prestamo> buscarPrestamosAltaUsuario(@Param("idUsuario") String idUsuario);

    // Método que devuelve el/los Prestamo/s dados de Baja de un Usuario:
    @Query("SELECT p FROM Prestamo p WHERE p.alta = false AND p.usuario.id = :idUsuario")
    public List<Prestamo> buscarPrestamosBajaUsuario(@Param("idUsuario") String idUsuario);

}
