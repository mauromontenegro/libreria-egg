package com.egg.libreriaEgg.servicios;

import com.egg.libreriaEgg.entidades.Autor;
import com.egg.libreriaEgg.entidades.Editorial;
import com.egg.libreriaEgg.entidades.Foto;
import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.entidades.Prestamo;
import com.egg.libreriaEgg.repositorios.AutorRepositorio;
import com.egg.libreriaEgg.repositorios.EditorialRepositorio;
import com.egg.libreriaEgg.repositorios.LibroRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Esta clase tiene la responsabilidad de llevar adelante las funcionalidades
 * necesarias para administrar libros (consulta, creación, modificación y dar de
 * baja).
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Service
public class LibroServicio {

    @Autowired
    private LibroRepositorio libroRepositorio;

    @Autowired
    private AutorRepositorio autorRepositorio;

    @Autowired
    private AutorServicio autorServicio;

    @Autowired
    private EditorialRepositorio editorialRepositorio;

    @Autowired
    private EditorialServicio editorialServicio;

    @Autowired
    private PrestamoServicio prestamoServicio;

    @Autowired
    private FotoServicio fotoServicio;

    /**
     * Método para registrar un libro.
     *
     * @param archivo --> foto
     * @param isbn
     * @param titulo
     * @param anio
     * @param descripcion
     * @param ejemplares
     * @param autor
     * @param editorial
     * @throws Exception
     */
    @Transactional
    public void agregarLibro(MultipartFile archivo, Long isbn, String titulo, Integer anio, String descripcion, Integer ejemplares, Autor autor, Editorial editorial) throws Exception {
        try {
            Libro libro = new Libro();
            // Valido los datos ingresados:
            validar(isbn, titulo, anio, descripcion, ejemplares);
            // Seteo de atributos:
            libro.setAlta(true);
            libro.setIsbn(isbn);
            libro.setTitulo(titulo);
            libro.setAnio(anio);
            libro.setDescripcion(descripcion);
            libro.setEjemplares(ejemplares);
            libro.setEjemplaresPrestados(0);
            libro.setEjemplaresRestantes(ejemplares);
            libro.setAutor(autor);
            // Se da de alta el autor en caso de que esté dado de baja:
            if (!libro.getAutor().isAlta()) {
                autorServicio.alta(libro.getAutor().getId());
            }
            libro.setEditorial(editorial);
            // Se da de alta la editorial en caso de que esté dada de baja:
            if (!libro.getEditorial().isAlta()) {
                editorialServicio.alta(libro.getEditorial().getId());
            }
            Foto foto = fotoServicio.guardar(archivo);
            libro.setFoto(foto);
            // Persistencia en la DB:
            libroRepositorio.save(libro);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
//            throw new Exception("Error al intentar guardar el Libro.");
        }
    }

    /**
     * Método para modificar un libro.
     *
     * @param id
     * @param archivo
     * @param isbn
     * @param titulo
     * @param anio
     * @param descripcion
     * @param ejemplares
     * @param autor
     * @param editorial
     * @throws Exception
     */
    @Transactional
    public void modificarLibro(String id, MultipartFile archivo, Long isbn, String titulo, Integer anio, String descripcion, Integer ejemplares, Autor autor, Editorial editorial) throws Exception {
        try {
            // Valido los datos ingresados:
            validar(isbn, titulo, anio, descripcion, ejemplares);
            // Usamos el repositorio para que busque el libro cuyo id sea el pasado como parámetro.
            Optional<Libro> respuesta = libroRepositorio.findById(id);
            if (respuesta.isPresent()) { // El Libro con ese id SI existe en la DB
                Libro libro = respuesta.get();
                // Seteo de atributos:
                libro.setIsbn(isbn);
                libro.setTitulo(titulo);
                libro.setAnio(anio);
                libro.setDescripcion(descripcion);
                // Contar los préstamos existentes al momento de la modificación:
                List<Prestamo> prestados = prestamoServicio.buscarPorLibro(id);
                libro.setEjemplaresPrestados(prestados.size());
                if (libro.getEjemplaresPrestados() > ejemplares) {
                    throw new Exception("Existen más préstamos vigentes que la cantidad de ejemplares que se indicó. Revise por favor los datos ingresados.");
                } else {
                    libro.setEjemplares(ejemplares);
                }
                libro.setEjemplaresRestantes(ejemplares - libro.getEjemplaresPrestados());
                // Seteo de Autor y Editorial:
                libro.setAutor(autor);
                libro.setEditorial(editorial);
                if (!archivo.isEmpty()) {
                    String idFoto = null;
                    if (libro.getFoto() != null) {
                        idFoto = libro.getFoto().getId();
                    }
                    Foto foto = fotoServicio.actualizar(idFoto, archivo);
                    libro.setFoto(foto);
                }
                // Persistencia en la DB:
                libroRepositorio.save(libro);
            } else { // El libro con ese id NO existe en la DB
                throw new Exception("No existe el Libro con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
//            throw new Exception("Error al intentar modificar el Libro.");
        }
    }

    /**
     * El método borra el libro de la DB (no se utiliza para darlo de baja).
     *
     * @param id
     * @throws java.lang.Exception
     */
    @Transactional
    public void eliminarLibro(String id) throws Exception {
        try {
            // Usamos el repositorio para que busque el libro cuyo id sea el pasado como parámetro.
            Optional<Libro> respuesta = libroRepositorio.findById(id);
            if (respuesta.isPresent()) { // El Libro con ese id SI existe en la DB
                Libro libro = respuesta.get();
                // Se eliminan todos los préstamos del libro:
                List<Prestamo> prestamosLibro = prestamoServicio.buscarPorLibro(id);
                for (Prestamo prestamo : prestamosLibro) {
                    prestamoServicio.eliminarPrestamo(prestamo.getId());
                }
                // Persistencia en la DB:
                libroRepositorio.delete(libro);
            } else { // El libro con ese id NO existe en la DB
                throw new Exception("No existe el Libro con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar eliminar el Libro.");
        }
    }

    /**
     * El método sirve para setear como 'false' el atributo 'alta' del Libro.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void baja(String id) throws Exception {
        try {
            // Usamos el repositorio para que busque el libro cuyo id sea el pasado como parámetro.
            Libro libro = libroRepositorio.getById(id);
            if (libro != null) { // El Libro con ese id SI existe en la DB
                // Se dan de baja los préstamos del libro:
                List<Prestamo> prestamosLibro = prestamoServicio.buscarPorLibro(id);
                for (Prestamo prestamo : prestamosLibro) {
                    if (prestamo.isAlta()) {
                        prestamoServicio.baja(prestamo.getId());
                    }
                }
                libro.setAlta(false);
                libroRepositorio.save(libro);
            } else { // El libro con ese id NO existe en la DB
                throw new Exception("No existe el Libro con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar dar de baja el Libro.");
        }
    }

    /**
     * El método sirve para setear como 'true' el atributo 'alta' del Libro.
     * También da de alta el autor, en caso de que esté dado de baja.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void alta(String id) throws Exception {
        try {
            // Usamos el repositorio para que busque el libro cuyo id sea el pasado como parámetro.
            Optional<Libro> respuesta = libroRepositorio.findById(id);
            if (respuesta.isPresent()) { // El Libro con ese id SI existe en la DB
                Libro libro = respuesta.get();
                libro.setAlta(true);
                libroRepositorio.save(libro);
                // Da de alta el autor y/o editorial (en caso de que estén dados de baja):
                if (!libro.getAutor().isAlta()) {
                    autorServicio.alta(libro.getAutor().getId());
                }
                if (!libro.getEditorial().isAlta()) {
                    editorialServicio.alta(libro.getEditorial().getId());
                }

            } else { // El libro con ese id NO existe en la DB
                throw new Exception("No existe el Libro con el id indicado.");
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar dar de alta el Libro.");
        }
    }

    /**
     * El método modifica los ejemplares prestados y restantes en caso de que
     * sea válido registrar el préstamo.
     *
     * @param libro
     * @throws Exception
     */
    @Transactional
    public void prestamoLibro(Libro libro) throws Exception {
        if (libro.getEjemplaresRestantes() >= 1) {
            libro.setEjemplaresPrestados(libro.getEjemplaresPrestados() + 1);
            libro.setEjemplaresRestantes(libro.getEjemplares() - libro.getEjemplaresPrestados());
        } else {
            throw new Exception("No hay suficientes ejemplares disponibles para realizar el préstamo.");
        }
    }

    /**
     * El método modifica los ejemplares prestados y restantes al realizar una
     * devolución.
     *
     * @param libro
     * @throws Exception
     */
    @Transactional
    public void devolucionLibro(Libro libro) throws Exception {
        if (libro.getEjemplaresPrestados() >= 1) {
            libro.setEjemplaresPrestados(libro.getEjemplaresPrestados() - 1);
            libro.setEjemplaresRestantes(libro.getEjemplares() - libro.getEjemplaresPrestados());
        } else {
            throw new Exception("No hay préstamos registrados para este Libro.");
        }
    }

    /**
     * No se tienen en cuenta ni el Autor ni la Editorial, ya que se podrán
     * seleccionar de una lista.
     *
     * @param isbn
     * @param titulo
     * @param anio
     * @param descripcion
     * @param ejemplares
     * @throws Exception
     */
    public void validar(Long isbn, String titulo, Integer anio, String descripcion, Integer ejemplares) throws Exception {

        if (isbn <= 0 || isbn == null) {
            throw new Exception("ISBN no válido.");
        }
        if (titulo == null || titulo.isEmpty()) {
            throw new Exception("Título no válido.");
        }
        if (anio <= 0 || anio == null) {
            throw new Exception("Año no válido.");
        }
        if (descripcion == null || descripcion.isEmpty()) {
            throw new Exception("La descripción es obligatoria.");
        }
        if (descripcion.length() > 255) {
            throw new Exception("La descripción no puede tener más de 200 caracteres.");
        }
        if (ejemplares < 0 || ejemplares == null) {
            throw new Exception("Cantidad de ejemplares no válidas.");
        }
    }

    // ------------------------------ MÉTODOS DEL REPOSITORIO ------------------------------
    /**
     *
     * @param id
     * @return
     */
    public Libro getById(String id) {
        return libroRepositorio.getById(id);
    }

    /**
     *
     * @param isbn del libro
     * @return
     */
    public Libro buscarPorIsbn(Long isbn) {
        return libroRepositorio.buscarPorIsbn(isbn);
    }

    /**
     *
     * @param idAutor
     * @return
     */
    public List<Libro> buscarPorAutor(String idAutor) {
        return libroRepositorio.buscarPorAutor(idAutor);
    }

    /**
     *
     * @param id
     * @return
     */
    public List<Libro> buscarPorEditorial(String id) {
        return libroRepositorio.buscarPorEditorial(id);
    }

    /**
     * Sólo devuelve los libros dados de alta.
     *
     * @return
     */
    public List<Libro> findAll() {
        return libroRepositorio.findAll();
    }

    /**
     * Sólo devuelve los libros dados de baja.
     *
     * @return
     */
    public List<Libro> listarDeBaja() {
        return libroRepositorio.listarDeBaja();
    }
}
