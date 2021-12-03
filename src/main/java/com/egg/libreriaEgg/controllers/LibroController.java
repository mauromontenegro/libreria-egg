package com.egg.libreriaEgg.controllers;

import com.egg.libreriaEgg.entidades.Autor;
import com.egg.libreriaEgg.entidades.Editorial;
import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.servicios.AutorServicio;
import com.egg.libreriaEgg.servicios.EditorialServicio;
import com.egg.libreriaEgg.servicios.LibroServicio;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador para gestionar todo lo relacionado a la entidad Libro (listar,
 * registrar, modificar, dar de baja/alta, eliminar). Sólo accesible por un
 * ADMIN.
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin/libros")
public class LibroController {

    @Autowired
    private LibroServicio libroServicio;

    @Autowired
    private AutorServicio autorServicio;

    @Autowired
    private EditorialServicio editorialServicio;

    /**
     * Muestra el Menú Administrativo de Libros, con los datos de todos los
     * libros, autores y editoriales inyectados al modelo.
     *
     * @param model
     * @return
     */
    @GetMapping("/admin-libros")
    public String administradorLibros(ModelMap model) {
        // Datos inyectados al modelo de "admin-libro.html":
        List<Libro> libros = libroServicio.findAll();
        model.put("libros", libros);
        List<Libro> librosDeBaja = libroServicio.listarDeBaja();
        model.put("librosDeBaja", librosDeBaja);
        List<Autor> autores = autorServicio.findAll();
        model.put("autores", autores);
        List<Editorial> editoriales = editorialServicio.findAll();
        model.put("editoriales", editoriales);
        return "admin-libro.html";
    }

    /**
     * Función para registrar un libro. Se usa un "try-catch" para verificar que
     * se haya elegido un Autor y una Editorial, ya sea de la lista o se haya
     * registrado uno nuevo.
     *
     * En el caso de que alguno de los otros campos se haya dejado vacío, se
     * lanza una excepción que lo notifica.
     *
     * @param model
     * @param id
     * @param archivo
     * @param isbn
     * @param titulo
     * @param anio
     * @param descripcion
     * @param ejemplares
     * @param idAutor
     * @param nuevoAutor
     * @param idEditorial
     * @param nuevaEditorial
     * @return
     */
    @PostMapping("/registrar-libro")
    public String registrarLibro(ModelMap model, @RequestParam(required = false) String id, MultipartFile archivo, Long isbn, String titulo, Integer anio, String descripcion, Integer ejemplares, String idAutor, String nuevoAutor, String idEditorial, String nuevaEditorial) {
        Autor autor;
        Editorial editorial;
        try {
            // Seteo del Autor:
            try {
                if (nuevoAutor == null || nuevoAutor.isEmpty()) {
                    autor = autorServicio.getById(idAutor);
                } else {
                    autorServicio.agregarAutor(nuevoAutor);
                    autor = autorServicio.buscarPorNombre(nuevoAutor);
                }
            } catch (Exception e) {
                throw new Exception("Debe seleccionar un Autor.");
            }
            // Seteo de la Editorial:
            try {
                if (nuevaEditorial == null || nuevaEditorial.isEmpty()) {
                    editorial = editorialServicio.getById(idEditorial);
                } else {
                    editorialServicio.agregarEditorial(nuevaEditorial);
                    editorial = editorialServicio.buscarPorNombre(nuevaEditorial);
                }
            } catch (Exception e) {
                throw new Exception("Debe seleccionar una Editorial.");
            }

            libroServicio.agregarLibro(archivo, isbn, titulo, anio, descripcion, ejemplares, autor, editorial);
            // Mensaje de éxito inyectado al modelo de "admin-libro.html":
            model.put("success", "El libro '" + libroServicio.buscarPorIsbn(isbn).getTitulo().toUpperCase() + "' fue registrado exitosamente.");
            // Datos inyectados al modelo de "admin-libro.html":
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Libro> librosDeBaja = libroServicio.listarDeBaja();
            model.put("librosDeBaja", librosDeBaja);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo de "error.html":
            if (e.getMessage() == null || isbn == null || anio == null || descripcion == null || ejemplares == null || idAutor == null || idEditorial == null) {
                model.put("error", "Error al intentar guardar el libro: faltó completar algún campo.");
            } else {
                model.put("error", "Error al intentar guardar el libro: " + e.getMessage());
            }
            // Datos inyectados al modelo de "admin-libro.html":
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Libro> librosDeBaja = libroServicio.listarDeBaja();
            model.put("librosDeBaja", librosDeBaja);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
        }
        return "admin-libro.html";
    }

    /**
     * Función que carga la vista para modificar los datos de un libro elegido
     * previamente. Busca el libro en el repositorio por id, y lo inyecta al
     * modelo para tener todos sus datos. También se inyecta la lista de Autores
     * y Editoriales.
     *
     * @param model
     * @param idLibroModif
     * @return
     */
    @GetMapping("/modificar-libro-datos/{idLibroModif}")
    public String datosLibro(ModelMap model,
            @PathVariable String idLibroModif
    ) {
        Libro libro = libroServicio.getById(idLibroModif);
        model.put("libroModif", libro);
        List<Autor> autores = autorServicio.findAll();
        model.put("autores", autores);
        List<Editorial> editoriales = editorialServicio.findAll();
        model.put("editoriales", editoriales);
        return "modif-libro.html";
    }

    /**
     * Función para modificar un libro.
     *
     * ESTE MÉTODO USA EL MODEL PARA QUE APAREZCA LA ALERTA ("success") EN LA
     * MISMA PLANTILA DE "admin-libro.html", O LA ALERTA ("error") EN LA
     * PLANTILLA DE "modif-libro.html".
     *
     * @param model
     * @param id
     * @param archivo
     * @param isbn
     * @param titulo
     * @param anio
     * @param descripcion
     * @param ejemplares
     * @param idAutor
     * @param idEditorial
     * @return
     */
    @PostMapping("/modificar-libro")
    public String modificarLibro(ModelMap model, @RequestParam String id, MultipartFile archivo, @RequestParam String isbn, @RequestParam String titulo, @RequestParam Integer anio, @RequestParam String descripcion, @RequestParam Integer ejemplares, @RequestParam String idAutor, @RequestParam String idEditorial) {

        try {
            Autor autor = autorServicio.getById(idAutor);
            Editorial editorial = editorialServicio.getById(idEditorial);
            if (isbn.isEmpty()) {
                throw new Exception("ISBN no válido.");
            }
            libroServicio.modificarLibro(id, archivo, Long.parseLong(isbn), titulo, anio, descripcion, ejemplares, autor, editorial);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "El libro '" + libroServicio.getById(id).getTitulo().toUpperCase() + "' fue modificado exitosamente.");
            // Datos inyectados al modelo:
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Libro> librosDeBaja = libroServicio.listarDeBaja();
            model.put("librosDeBaja", librosDeBaja);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
            return "admin-libro.html";
        } catch (Exception e) {
            if (e.getMessage() == null || isbn == null || anio == null || descripcion == null || ejemplares == null || idAutor == null || idEditorial == null) {
                model.put("error", "Error al intentar modificar el libro: faltó completar algún campo.");
            } else {
                model.put("error", "Error al intentar modificar el libro: " + e.getMessage());
            }
            // Datos inyectados al modelo de "admin-libro.html":
            Libro libro = libroServicio.getById(id);
            model.put("libroModif", libro);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
            return "modif-libro.html";
        }
    }

    /**
     * Función para eliminar un libro. Antes de eliminarlo desde el servicio,
     * capturo el titulo en una variable para poder utilizarlo en el mensaje de
     * "success". Es una url con "path variable" (id del libro a eliminar).
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/eliminar-libro/{id}")
    public String eliminarLibro(ModelMap model, @PathVariable String id) {
        try {
            String titulo = libroServicio.getById(id).getTitulo().toUpperCase();
            // Con el id, llamo al método para eliminar el libro:
            libroServicio.eliminarLibro(id);
            // Mensaje de éxito inyectado al modelo de "exito.html":
            model.put("success", "El libro '" + titulo + "' fue eliminado exitosamente.");
            // Datos inyectados al modelo de "admin-libro.html":
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Libro> librosDeBaja = libroServicio.listarDeBaja();
            model.put("librosDeBaja", librosDeBaja);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo:
            model.put("error", "Error al intentar eliminar el libro: " + e.getMessage());
            // Datos inyectados al modelo de "admin-libro.html":
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
        }
        return "admin-libro.html";
    }

    /**
     * Función para dar de baja un libro. Una vez modificado el atributo "alta"
     * desde el servicio, muestra la página de "admin-libro.html" con mensajes
     * inyectados al modelo. Es una url con "path variable" (id del libro a dar
     * de baja).
     *
     * ESTE MÉTODO USA EL MODEL PARA QUE APAREZCA LA ALERTA ("success" O
     * "error") EN LA MISMA PLANTILA DE "admin-libro.html".
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/baja/{id}")
    public String baja(ModelMap model, @PathVariable String id) {
        try {
            libroServicio.baja(id);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "El libro '" + libroServicio.getById(id).getTitulo().toUpperCase() + "' fue dado de baja exitosamente.");
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo:
            model.put("error", "Error al intentar dar de baja el libro: " + e.getMessage());
        }
        // Datos inyectados al modelo de "admin-libro.html":
        List<Libro> libros = libroServicio.findAll();
        model.put("libros", libros);
        List<Libro> librosDeBaja = libroServicio.listarDeBaja();
        model.put("librosDeBaja", librosDeBaja);
        List<Autor> autores = autorServicio.findAll();
        model.put("autores", autores);
        List<Editorial> editoriales = editorialServicio.findAll();
        model.put("editoriales", editoriales);
        return "admin-libro.html";
    }

    /**
     * Función para dar de alta un libro. Una vez modificado el atributo "alta"
     * desde el servicio, muestra la página de "exito" o "error" con mensajes
     * inyectados al modelo. Es una url con "path variable" (id del libro a dar
     * de alta).
     *
     * ESTE MÉTODO USA EL MODEL PARA QUE APAREZCA LA ALERTA ("success" O
     * "error") EN LA MISMA PLANTILA DE "admin-libro.html".
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/alta/{id}")
    public String alta(ModelMap model, @PathVariable String id) {
        try {
            Autor autor = libroServicio.getById(id).getAutor();
            if (autor.isAlta()) {
                // Mensaje de éxito inyectado al modelo de "exito.html":
                model.put("success", "El libro '" + libroServicio.getById(id).getTitulo().toUpperCase() + "' fue dado de alta exitosamente.");
            } else {
                model.put("success", "El libro '" + libroServicio.getById(id).getTitulo().toUpperCase() + "' fue dado de alta exitosamente,"
                        + " al igual que su autor '" + autor.getNombre().toUpperCase() + "'.");
            }
            libroServicio.alta(id);
            // Datos inyectados al modelo de "admin-libro.html":
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Libro> librosDeBaja = libroServicio.listarDeBaja();
            model.put("librosDeBaja", librosDeBaja);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
            return "admin-libro.html";
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo de "error.html":
            model.put("error", "Error al intentar dar de alta el libro: " + e.getMessage());
            // Datos inyectados al modelo de "admin-libro.html":
            List<Libro> libros = libroServicio.findAll();
            model.put("libros", libros);
            List<Autor> autores = autorServicio.findAll();
            model.put("autores", autores);
            List<Editorial> editoriales = editorialServicio.findAll();
            model.put("editoriales", editoriales);
            return "admin-libro.html";
        }
    }
}
