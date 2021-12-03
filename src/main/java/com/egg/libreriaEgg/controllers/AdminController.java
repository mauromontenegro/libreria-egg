package com.egg.libreriaEgg.controllers;

import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.entidades.Prestamo;
import com.egg.libreriaEgg.entidades.Usuario;
import com.egg.libreriaEgg.servicios.LibroServicio;
import com.egg.libreriaEgg.servicios.PrestamoServicio;
import com.egg.libreriaEgg.servicios.UsuarioServicio;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para gestionar todas las funciones para el rol de ADMIN en
 * relación a Usuarios y Préstamos.
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private PrestamoServicio prestamoServicio;

    @Autowired
    private LibroServicio libroServicio;

    /**
     * Trae la vista del Dashboard. Incluye una tabla (para lo cual se pasan
     * todos los usuarios a través del model) con la lista de usuarios y
     * distintas opciones para gestionarlos.
     *
     * @param model
     * @return
     */
    @GetMapping("/dashboard")
    public String homeAdmin(ModelMap model) {
        // Pasamos la lista de todos los usuarios para la tabla del dashboard:
        List<Usuario> usuariosActivos = usuarioServicio.buscarActivos();
        model.addAttribute("usuariosActivos", usuariosActivos);
        List<Usuario> usuariosInactivos = usuarioServicio.buscarInactivos();
        model.addAttribute("usuariosInactivos", usuariosInactivos);
        return "admin.html";
    }

    /**
     * Método para eliminar un usuario a partir de un @PathVariable
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(ModelMap model, @PathVariable String id) {
        try {
            usuarioServicio.eliminar(id);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error al intentar eliminar el usuario.");
            return "admin.html";
        }
    }

    /**
     * Método para dar de alta un usuario a partir de un @PathVariable.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/habilitar/{id}")
    public String habilitar(ModelMap model, @PathVariable String id) {
        try {
            usuarioServicio.habilitar(id);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error al intentar dar de alta al usuario.");
            return "admin.html";
        }
    }

    /**
     * Método para dar de baja un usuario a partir de un @PathVariable.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/deshabilitar/{id}")
    public String deshabilitar(ModelMap model, @PathVariable String id) {
        try {
            usuarioServicio.deshabilitar(id);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error al intentar dar de baja al usuario.");
            return "admin.html";
        }
    }

    /**
     * Método para cambiar el rol de un usuario a partir de un @PathVariable
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/cambiar-rol/{id}")
    public String cambiarRol(ModelMap model, @PathVariable String id) {
        try {
            usuarioServicio.cambiarRol(id);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error al intentar modificar el rol.");
            return "admin.html";
        }
    }

    /**
     * Muestra el Menú Administrativo de Préstamos, con los datos de todos los
     * Préstamos inyectados al modelo.
     *
     * @param model
     * @return
     */
    @GetMapping("/prestamos/admin-prestamos")
    public String administradorPrestamos(ModelMap model) {
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAlta();
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBaja();
        model.addAttribute("prestamosBaja", prestamosBaja);
        return "admin-prestamo.html";
    }

    /**
     * Función para registrar un Préstamo a nombre de un Usuario.
     *
     * @param model
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @param idLibro
     * @param idUsuario
     * @return
     * @throws ParseException
     */
    @PostMapping("/prestamos/registrar-prestamo")
    public String registrarPrestamo(ModelMap model, @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaPrestamo, @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDevolucion, String idLibro, String idUsuario) throws ParseException {
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
                throw new Exception("Límite de 4 prestamos activos alcanzado por el Usuario. Debe registrar una devolución para solicitar un nuevo préstamo.");
            } else {
                // Registro del Préstamo:
                prestamoServicio.agregarPrestamo(fechaPrestamo, fechaDevolucion, libro.getId(), usuario.getId());
                // Mensaje de éxito:
                model.addAttribute("success", "El préstamo del libro '" + libro.getTitulo().toUpperCase() + "' al usuario '" + usuario.getNombre().toUpperCase() + " " + usuario.getApellido().toUpperCase() + "' fue registrado exitosamente. "
                        + "Quedan " + libro.getEjemplaresRestantes() + " ejemplares disponibles.");
            }
        } catch (Exception e) {
            if (e.getMessage() == null || fechaPrestamo == null || fechaDevolucion == null || idLibro == null) {
                model.addAttribute("error", "Error al intentar registrar Préstamo: faltó completar algún campo.");
            } else {
                model.addAttribute("error", "Error al registrar Préstamo: " + e.getMessage());
            }
        }
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAlta();
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBaja();
        model.addAttribute("prestamosBaja", prestamosBaja);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        return "admin-prestamo.html";
    }

    /**
     * Método para enviar el id del usuario a la vista para crear nuevo
     * préstamo.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/prestamos/registar-prestamo-usuario/{id}")
    public String registrarPrestamoUsuario(ModelMap model, @PathVariable String id) {
        Usuario usuario = usuarioServicio.getById(id);
        model.addAttribute("usuarioSelected", usuario);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAlta();
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBaja();
        model.addAttribute("prestamosBaja", prestamosBaja);
        return "admin-prestamo.html";
    }

    /**
     * Filtra los préstamos de un sólo usuario. Utiliza la vista de
     * "admin-prestamo.html".
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/prestamos/admin-prestamos-usuario/{id}")
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
        return "admin-prestamo.html";
    }

    /**
     * Registra la devolución del libro, dando de baja el préstamo.
     *
     * @param model
     * @param idPrestamo
     * @return
     * @throws Exception
     */
    @GetMapping("/prestamos/registrar-devolucion/{idPrestamo}")
    public String registrarDevolucion(ModelMap model, @PathVariable String idPrestamo) throws Exception {
        try {
            Prestamo prestamo = prestamoServicio.buscarPorId(idPrestamo);
            Libro libro = prestamo.getLibro();
            Usuario usuario = prestamo.getUsuario();
            prestamoServicio.baja(idPrestamo);
            model.addAttribute("success", "La devolución del libro '" + libro.getTitulo().toUpperCase() + "' del usuario '" + usuario.getNombre().toUpperCase() + " " + usuario.getApellido().toUpperCase() + "' fue registrada exitosamente. "
                    + "Quedan " + libro.getEjemplaresRestantes() + " ejemplares disponibles.");
        } catch (Exception e) {
            if (e.getMessage() == null) {
                model.addAttribute("error", "Error al intentar registrar Devolución. Intente nuevamente.");
            } else {
                model.addAttribute("error", "Error al registrar Devolución: " + e.getMessage());
            }
        }
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAlta();
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBaja();
        model.addAttribute("prestamosBaja", prestamosBaja);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        return "admin-prestamo.html";
    }

    /**
     * Precarga los datos del prestamo en la vista, para poder dar paso a la
     * renovación del mismo.
     *
     * @param model
     * @param idPrestamoModif
     * @return
     */
    @GetMapping("/prestamos/modificar-prestamo-datos/{idPrestamoModif}")
    public String datosPrestamo(ModelMap model, @PathVariable String idPrestamoModif) {
        Prestamo prestamo = prestamoServicio.buscarPorId(idPrestamoModif);
        model.addAttribute("prestamoModif", prestamo);
        return "modif-prestamo.html";
    }

    /**
     * SÓLO SIRVE PARA RENOVAR EL PRÉSTAMO (fecha de préstamo y fecha de
     * devolución). No se puede modificar libro ni usuario.
     *
     * @param model
     * @param id
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @return
     * @throws ParseException
     */
    @PostMapping("/prestamos/modificar-prestamo")
    public String modificarPrestamo(ModelMap model, @RequestParam String id, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaPrestamo, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDevolucion) throws ParseException {
        Libro libro = prestamoServicio.buscarPorId(id).getLibro();
        Usuario usuario = prestamoServicio.buscarPorId(id).getUsuario();
        try {
            // Modificación del Préstamo:
            prestamoServicio.modificarPrestamo(id, fechaPrestamo, fechaDevolucion);
            model.addAttribute("success", "El préstamo del libro '" + libro.getTitulo().toUpperCase() + "' al usuario '" + usuario.getNombre().toUpperCase() + " " + usuario.getApellido().toUpperCase() + "' fue modificado exitosamente. "
                    + "Quedan " + libro.getEjemplaresRestantes() + " ejemplares disponibles.");
        } catch (Exception e) {
            if (e.getMessage() == null || fechaPrestamo == null || fechaDevolucion == null) {
                model.addAttribute("error", "Error al intentar modificar Préstamo: faltó completar algún campo.");
            } else {
                model.addAttribute("error", "Error al modificar Préstamo: " + e.getMessage());
            }
        }
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAlta();
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBaja();
        model.addAttribute("prestamosBaja", prestamosBaja);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        return "admin-prestamo.html";
    }

    /**
     * Función para eliminar un préstamo de la base de datos.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/prestamos/eliminar-prestamo/{id}")
    public String eliminarPrestamo(ModelMap model, @PathVariable String id) {
        try {
            Libro libro = prestamoServicio.buscarPorId(id).getLibro();
            String tituloLibro = libro.getTitulo().toUpperCase();
            String nombreUsuario = prestamoServicio.buscarPorId(id).getUsuario().getNombre().toUpperCase() + ' ' + prestamoServicio.buscarPorId(id).getUsuario().getApellido().toUpperCase();
            prestamoServicio.eliminarPrestamo(id);
            model.addAttribute("success", "El préstamo del libro '" + tituloLibro + "' al usuario '" + nombreUsuario + "' fue eliminado exitosamente. "
                    + "Quedan " + libro.getEjemplaresRestantes() + " ejemplares disponibles.");
        } catch (Exception e) {
            if (e.getMessage() == null) {
                model.addAttribute("error", "Error al intentar eliminar Préstamo: faltó completar algún campo.");
            } else {
                model.addAttribute("error", "Error al eliminar Préstamo: " + e.getMessage());
            }
        }
        List<Prestamo> prestamosAlta = prestamoServicio.listarDeAlta();
        model.addAttribute("prestamosAlta", prestamosAlta);
        List<Prestamo> prestamosBaja = prestamoServicio.listarDeBaja();
        model.addAttribute("prestamosBaja", prestamosBaja);
        List<Libro> libros = libroServicio.findAll();
        model.addAttribute("libros", libros);
        List<Usuario> usuarios = usuarioServicio.buscarActivos();
        model.addAttribute("usuarios", usuarios);
        return "admin-prestamo.html";
    }
}
