package com.egg.libreriaEgg.controllers;

import com.egg.libreriaEgg.entidades.Autor;
import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.entidades.Prestamo;
import com.egg.libreriaEgg.entidades.Usuario;
import com.egg.libreriaEgg.servicios.AutorServicio;
import com.egg.libreriaEgg.servicios.LibroServicio;
import com.egg.libreriaEgg.servicios.PrestamoServicio;
import com.egg.libreriaEgg.servicios.UsuarioServicio;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la gestión de préstamos para usuarios con el Rol "USUARIO".
 * Se permite la visualización de los préstamos registrados a su nombre, y la
 * solicitud de préstamos de los libros dados de alta. Se limita la gestión de
 * préstamos a un máximo de 4 activos. No se pueden gestionar devoluciones ni
 * renovaciones.
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoServicio prestamoServicio;

    @Autowired
    private LibroServicio libroServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private AutorServicio autorServicio;

    /**
     * Método para registrar la solicitud de un préstamo por un usuario.
     *
     * @param model
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @param idLibro
     * @param idUsuario
     * @return
     * @throws ParseException
     */
    @PostMapping("/registrar-prestamo")
    public String registrarPrestamo(ModelMap model, @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaPrestamo, @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDevolucion, String idLibro, String idUsuario) throws ParseException, Exception {
        Libro libro;
        Usuario usuario;
        try {
            // Seteo del Libro:
            libro = libroServicio.getById(idLibro);
            // Seteo del Usuario:
            usuario = usuarioServicio.getById(idUsuario);
            // Validación: un usuario no puede exceder los 4 préstamos activos.
            List<Prestamo> prestamosUsuario = prestamoServicio.listarDeAltaUsuario(idUsuario);
            if (prestamosUsuario.size() >= 4) {
                throw new Exception("Límite de 4 prestamos activos alcanzado. Debe registrar una devolución para solicitar un nuevo préstamo.");
            } else {
                // Registro del Préstamo:
                prestamoServicio.agregarPrestamo(fechaPrestamo, fechaDevolucion, libro.getId(), usuario.getId());
                // Mensaje de éxito:
                model.addAttribute("success", "El préstamo del libro '" + libro.getTitulo().toUpperCase() + "' fue registrado exitosamente. "
                        + "Quedan " + libro.getEjemplaresRestantes() + " ejemplares disponibles.");
            }
        } catch (Exception e) {
            if (e.getMessage() == null || fechaDevolucion == null || idLibro == null) {
                model.addAttribute("error", "Error al intentar registrar Préstamo: faltó completar algún campo.");
            } else {
                model.addAttribute("error", "Error al registrar Préstamo: " + e.getMessage());
            }
        }
        List<Autor> autores = autorServicio.findAll();
        model.addAttribute("autores", autores);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        return "inicio.html";
    }

    /**
     * Filtra todos los préstamos de un sólo usuario. Utiliza la vista de
     * "prestamos-cliente.html".
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/admin-prestamos-usuario/{id}")
    public String verPrestamosUsuario(ModelMap model, @PathVariable String id) {
        Usuario usuario = usuarioServicio.getById(id);
        model.addAttribute("usuarioPrestamo", usuario);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAltaUsuario(id);
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBajaUsuario(id);
        model.addAttribute("prestamosBaja", prestamosBaja);
        return "prestamos-cliente.html";
    }
}
