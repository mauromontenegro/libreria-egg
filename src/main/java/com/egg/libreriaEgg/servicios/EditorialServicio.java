package com.egg.libreriaEgg.servicios;

import com.egg.libreriaEgg.entidades.Editorial;
import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.repositorios.EditorialRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Esta clase tiene la responsabilidad de llevar adelante las funcionalidades
 * necesarias para administrar editoriales (consulta, creación, modificación y
 * dar de baja).
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Service
public class EditorialServicio {

    @Autowired
    private EditorialRepositorio editorialRepositorio;

    @Autowired
    private LibroServicio libroServicio;

    /**
     * Método para registrar una Editorial.
     *
     * @param nombre
     * @throws Exception
     */
    @Transactional
    public void agregarEditorial(String nombre) throws Exception {
        try {
            Editorial editorial = new Editorial();
            // Valido los datos ingresados:
            validar(nombre);
            // Seteo de atributos:
            editorial.setAlta(true);
            editorial.setNombre(nombre);
            // Persistencia en la DB:
            editorialRepositorio.save(editorial);
        } catch (Exception e) {
            throw new Exception("Error al intentar guardar la editorial.");
        }
    }

    /**
     * Método para modificar una Editorial.
     *
     * @param id
     * @param nombre
     * @throws Exception
     */
    @Transactional
    public void modificarEditorial(String id, String nombre) throws Exception {
        try {
            // Valido los datos ingresados:
            validar(nombre);
            Optional<Editorial> respuesta = editorialRepositorio.findById(id);
            if (respuesta.isPresent()) { // La Editorial con ese id SI existe en la DB
                Editorial editorial = respuesta.get();
                // Seteo de atributos:
                editorial.setNombre(nombre);
                // Persistencia en la DB:
                editorialRepositorio.save(editorial);
            } else { // La Editorial con ese id NO existe en la DB
                throw new Exception("No existe la Editorial con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar modificar la Editorial.");
        }
    }

    /**
     * El método borra la editorial de la DB (no se utiliza para darla de baja).
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void eliminarEditorial(String id) throws Exception {
        try {
            Editorial editorial = editorialRepositorio.getById(id);
            if (editorial != null) { // La editorial con ese id SI existe en la DB
                // Eliminar todos sus libros:
                List<Libro> libros = libroServicio.buscarPorEditorial(editorial.getId());
                for (Libro libro : libros) {
                    libroServicio.eliminarLibro(libro.getId());
                }
                // Persistencia en la DB:
                editorialRepositorio.delete(editorial);
            } else { // La editorial con ese id NO existe en la DB
                throw new Exception("No existe la editorial con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar eliminar la editorial.");
        }
    }

    /**
     * El método sirve para setear como 'false' el atributo 'alta' de Editorial.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void baja(String id) throws Exception {
        try {
            Editorial editorial = editorialRepositorio.getById(id);
            if (editorial != null) { // la editorial con ese id SI existe en la DB
                // Dar de baja todos sus libros:
                List<Libro> libros = libroServicio.buscarPorEditorial(id);
                if (libros != null) {
                    for (Libro libro : libros) {
                        if (libro.isAlta()) {
                            libroServicio.baja(libro.getId());
                        }
                    }
                }
                editorial.setAlta(false);
                // Persistencia en la DB:
                editorialRepositorio.save(editorial);
            } else { // La editorial con ese id NO existe en la DB
                throw new Exception("No existe la editorial con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar dar de baja la editorial.");
        }
    }

    /**
     * El método sirve para setear como 'true' el atributo 'alta' de Editorial.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void alta(String id) throws Exception {
        try {
            Editorial editorial = editorialRepositorio.getById(id);
            if (editorial != null) { // la editorial con ese id SI existe en la DB
                editorial.setAlta(true);
                // Persistencia en la DB:
                editorialRepositorio.save(editorial);
            } else { // La editorial con ese id NO existe en la DB
                throw new Exception("No existe la editorial con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar dar de alta la editorial.");
        }
    }

    // ------------------------------ MÉTODOS DEL REPOSITORIO ------------------------------
    /**
     *
     * @param nombre
     * @throws Exception
     */
    public void validar(String nombre) throws Exception {
        if (nombre == null) {
            throw new Exception("Nombre no válido.");
        }
    }

    /**
     *
     * @param nombre
     * @return
     */
    public Editorial buscarPorNombre(String nombre) {
        return editorialRepositorio.buscarPorNombre(nombre);
    }

    /**
     *
     * @return
     */
    public List<Editorial> findAll() {
        return editorialRepositorio.findAll();
    }

    /**
     *
     * @param id
     * @return
     */
    public Editorial getById(String id) {
        return editorialRepositorio.getById(id);
    }

}
