package com.egg.libreriaEgg.repositorios;

import com.egg.libreriaEgg.entidades.Editorial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * (EditorialRepositorio) debe contener los métodos necesarios para
 * guardar/actualizar una editorial en la base de datos, realizar consultas o
 * dar de baja según corresponda. Extiende de JpaRepository: será un repositorio
 * de Editorial con la Primary Key de tipo String.
 *
 * Los métodos save(), findById() y delete() se implementan por JpaRepository.
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Repository
public interface EditorialRepositorio extends JpaRepository<Editorial, String> {

    // Método que devuelve la Editorial buscado por su nombre:
    @Query("SELECT e FROM Editorial e WHERE e.nombre = :nombre")
    public Editorial buscarPorNombre(@Param("nombre") String nombre);
    
    // Método que devuelve todos las editoriales, ordenadas alfabéticamente.
    @Query("SELECT e FROM Editorial e ORDER BY e.nombre ASC")
    @Override
    public List<Editorial> findAll();
}
