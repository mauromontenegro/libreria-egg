package com.egg.libreriaEgg.servicios;

import com.egg.libreriaEgg.entidades.Autor;
import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.repositorios.AutorRepositorio;
import com.egg.libreriaEgg.repositorios.LibroRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Esta clase tiene la responsabilidad de llevar adelante las funcionalidades
 * necesarias para administrar autores (consulta, creación, modificación y dar
 * de baja).
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Service
public class AutorServicio {

    @Autowired
    private AutorRepositorio autorRepositorio;

    @Autowired
    private LibroRepositorio libroRepositorio;

    @Autowired
    private LibroServicio libroServicio;

    /**
     * Método para registrar un autor.
     *
     * @param nombre
     * @throws Exception
     */
    @Transactional
    public void agregarAutor(String nombre) throws Exception {
        try {
            Autor autor = new Autor();
            // Valido los datos ingresados:
            validar(nombre);
            // Seteo de atributos:
            autor.setAlta(true);
            autor.setNombre(nombre);
            // Persistencia en la DB:
            autorRepositorio.save(autor);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Método para modificar un autor.
     *
     * @param id
     * @param nombre
     * @throws Exception
     */
    @Transactional
    public void modificarAutor(String id, String nombre) throws Exception {
        try {
            // Valido los datos ingresados:
            validar(nombre);
            Optional<Autor> respuesta = autorRepositorio.findById(id);
            if (respuesta.isPresent()) { // El autor con ese id SI existe en la DB
                Autor autor = respuesta.get();
                // Seteo de atributos:
                autor.setNombre(nombre);
                // Persistencia en la DB:
                autorRepositorio.save(autor);
            } else { // El autor con ese id NO existe en la DB
                throw new Exception("No existe el autor con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * El método borra el autor de la DB (no se utiliza para darlo de baja).
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void eliminarAutor(String id) throws Exception {
        try {
            Autor autor = autorRepositorio.getById(id);
            if (autor != null) { // El autor con ese id SI existe en la DB
                // Eliminar todos sus libros:
                List<Libro> libros = libroServicio.buscarPorAutor(id);
                for (Libro libro : libros) {
                    libroServicio.eliminarLibro(libro.getId());
                }
                // Persistencia en la DB:
                autorRepositorio.delete(autor);
            } else { // El autor con ese id NO existe en la DB
                throw new Exception("No existe el autor con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * El método sirve para setear como 'false' el atributo 'alta' del Autor.
     * Además, da de baja todos los libros escritos por dicho autor.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void baja(String id) throws Exception {
        try {
            Autor autor = autorRepositorio.getById(id);
            if (autor != null) { // El autor con ese id SI existe en la DB
                // Dar de baja todos sus libros:
                List<Libro> libros = libroServicio.buscarPorAutor(id);
                if (libros != null) {
                    for (Libro libro : libros) {
                        if (libro.isAlta()) {
                            libroServicio.baja(libro.getId());
                        }
                    }
                }
                autor.setAlta(false);
                // Persistencia en la DB:
                autorRepositorio.save(autor);
            } else { // El autor con ese id NO existe en la DB
                throw new Exception("No existe el autor con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar dar de baja el Autor.");
        }
    }

    /**
     * El método sirve para setear como 'true' el atributo 'alta' del Autor.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void alta(String id) throws Exception {
        try {
            Optional<Autor> respuesta = autorRepositorio.findById(id);
            if (respuesta.isPresent()) { // El autor con ese id SI existe en la DB
                Autor autor = respuesta.get();
                autor.setAlta(true);
                // Persistencia en la DB:
                autorRepositorio.save(autor);
                // Dar de alta todos sus libros:
                List<Libro> libros = libroRepositorio.buscarPorAutor(id);
                for (Libro libro : libros) {
                    libroServicio.alta(libro.getId());
                }
            } else { // El autor con ese id NO existe en la DB
                throw new Exception("No existe el autor con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar dar de alta el Autor.");
        }
    }

// ------------------------------ MÉTODOS DEL REPOSITORIO ------------------------------
    /**
     *
     * @param nombre
     * @throws Exception
     */
    public void validar(String nombre) throws Exception {
        if (nombre == null || nombre.isEmpty()) {
            throw new Exception("Nombre no válido.");
        }
    }

    /**
     *
     * @param nombre
     * @return
     */
    public Autor buscarPorNombre(String nombre) {
        return autorRepositorio.buscarPorNombre(nombre);
    }

    /**
     *
     * @return
     */
    public List<Autor> findAll() {
        return autorRepositorio.findAll();
    }

    /**
     *
     * @param id
     * @return
     */
    public Autor getById(String id) {
        return autorRepositorio.getById(id);
    }
}
