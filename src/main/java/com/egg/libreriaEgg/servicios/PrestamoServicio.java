package com.egg.libreriaEgg.servicios;

import com.egg.libreriaEgg.entidades.Libro;
import com.egg.libreriaEgg.entidades.Prestamo;
import com.egg.libreriaEgg.repositorios.PrestamoRepositorio;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Esta clase tiene la responsabilidad de llevar adelante las funcionalidades
 * necesarias para administrar préstamos (consulta, préstamo, modificación y dar
 * de baja).
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Service
public class PrestamoServicio {

    @Autowired
    private PrestamoRepositorio prestamoRepositorio;

    @Autowired
    private LibroServicio libroServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    /**
     * Método para registrar un Préstamo.
     *
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @param idLibro
     * @param idUsuario
     * @throws Exception
     */
    @Transactional
    public void agregarPrestamo(Date fechaPrestamo, Date fechaDevolucion, String idLibro, String idUsuario) throws Exception {
        Prestamo prestamo = new Prestamo();
        try {
            // Valido los datos ingresados:
            validar(fechaPrestamo, fechaDevolucion, idLibro, idUsuario);
            // Seteo de atributos:
            prestamo.setFechaPrestamo(fechaPrestamo);
            prestamo.setFechaDevolucion(fechaDevolucion);
            prestamo.setUsuario(usuarioServicio.getById(idUsuario));
            prestamo.setAlta(true);
            // Validación de ejemplares y seteo del Libro:
            try {
                Libro libro = libroServicio.getById(idLibro);
                validarEjemplaresLibroPrestamo(libro);
                prestamo.setLibro(libro);
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
            // Persistencia en la DB:
            prestamoRepositorio.save(prestamo);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * SÓLO SIRVE PARA RENOVAR EL PRÉSTAMO (fecha de préstamo y fecha de
     * devolución). No se puede modificar libro ni usuario.
     *
     * @param id
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @throws Exception
     */
    @Transactional
    public void modificarPrestamo(String id, Date fechaPrestamo, Date fechaDevolucion) throws Exception {
        try {
            // Valido los datos ingresados:
            validarFechas(fechaPrestamo, fechaDevolucion);
            // Usamos el repositorio para que busque el prestamo cuyo id sea el pasado como parámetro.
            Prestamo prestamo = prestamoRepositorio.getById(id);
            if (prestamo != null) {
                // Seteo de atributos:
                prestamo.setFechaPrestamo(fechaPrestamo);
                prestamo.setFechaDevolucion(fechaDevolucion);
                // Persistencia en la DB:
                prestamoRepositorio.save(prestamo);
            } else {
                throw new Exception("No existe el prestamo vinculado a ese ID.");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * El método borra el préstamo de la DB (no se utiliza para darlo de baja).
     * Si el préstamo está dado de alta, primero se registra la devolución del
     * libro; sino, simplemente se elimina el registro.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void eliminarPrestamo(String id) throws Exception {
        try {
            // Usamos el repositorio para que busque el prestamo cuyo id sea el pasado como parámetro.
            Prestamo prestamo = prestamoRepositorio.getById(id);
            if (prestamo.isAlta()) {
                Libro libro = prestamo.getLibro();
                libroServicio.devolucionLibro(libro);
                // Persistencia en la DB:
                prestamoRepositorio.delete(prestamo);
            } else {
                // Persistencia en la DB:
                prestamoRepositorio.delete(prestamo);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * El método sirve para setear como 'false' el atributo 'alta' del prestamo.
     * Se usa para registrar una devolución (a través de libroServicio),
     * modificando la fecha de devolución a la fecha actual en la que se llama
     * al método.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void baja(String id) throws Exception {
        try {
            // Usamos el repositorio para que busque el prestamo cuyo id sea el pasado como parámetro.
            Prestamo prestamo = prestamoRepositorio.getById(id);
            if (prestamo != null) {
                Libro libro = prestamo.getLibro();
                libroServicio.devolucionLibro(libro);
                prestamo.setFechaDevolucion(new Date());
                prestamo.setAlta(false);
                // Persistencia en la DB:
                prestamoRepositorio.save(prestamo);
            } else {
                throw new Exception("No existe el prestamo vinculado a ese ID.");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * El método sirve para setear como 'true' el atributo 'alta' del prestamo.
     * No se utiliza.
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    public void alta(String id) throws Exception {
        try {
            // Usamos el repositorio para que busque el prestamo cuyo id sea el pasado como parámetro.
            Prestamo prestamo = prestamoRepositorio.getById(id);
            if (prestamo != null) {
                prestamo.setAlta(true);
                // Persistencia en la DB:
                prestamoRepositorio.save(prestamo);
            } else {
                throw new Exception("No existe el prestamo vinculado a ese ID.");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Método para validar datos, creado para no repetir la lógica en otros
     * métodos:
     *
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @param idLibro
     * @param idUsuario
     * @throws Exception
     */
    public void validar(Date fechaPrestamo, Date fechaDevolucion, String idLibro, String idUsuario) throws Exception {
        if (fechaPrestamo == null) {
            throw new Exception("Fecha de Préstamo no válida.");
        }
        if (fechaDevolucion == null) {
            throw new Exception("Fecha de Devolución no válida.");
        }
        if (fechaPrestamo.after(fechaDevolucion)) {
            throw new Exception("La fecha de retiro del Libro ingresada es posterior a la de devolución.");
        }
        if (idLibro == null || libroServicio.getById(idLibro) == null) {
            throw new Exception("Id de Libro no válido.");
        }
        if (idUsuario == null || usuarioServicio.getById(idUsuario) == null) {
            throw new Exception("Id de Usuario no válido.");
        }
    }

    /**
     * Método para validar las fechas al renovar préstamo.
     *
     * @param fechaPrestamo
     * @param fechaDevolucion
     * @throws Exception
     */
    public void validarFechas(Date fechaPrestamo, Date fechaDevolucion) throws Exception {
        if (fechaPrestamo == null) {
            throw new Exception("Fecha de Préstamo no válida.");
        }
        if (fechaDevolucion == null) {
            throw new Exception("Fecha de Devolución no válida.");
        }
        if (fechaPrestamo.after(fechaDevolucion)) {
            throw new Exception("La fecha de retiro del Libro ingresada es posterior a la de devolución.");
        }
    }

    /**
     * Sirve para validar que existan ejemplares disponibles para llevar a cabo
     * el préstamo.
     *
     * @param libro
     * @throws Exception
     */
    public void validarEjemplaresLibroPrestamo(Libro libro) throws Exception {
        try {
            libroServicio.prestamoLibro(libro);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // ------------------------------ MÉTODOS DEL REPOSITORIO ------------------------------
    /**
     *
     * @param id
     * @return
     */
    public Prestamo buscarPorId(String id) {
        return prestamoRepositorio.getById(id);
    }

    /**
     *
     * @param idLibro
     * @return
     */
    public List<Prestamo> buscarPorLibro(String idLibro) {
        return prestamoRepositorio.buscarPorLibro(idLibro);
    }

    /**
     *
     * @param idUsuario
     * @return
     */
    public List<Prestamo> buscarPorUsuario(String idUsuario) {
        return prestamoRepositorio.buscarPorUsuario(idUsuario);
    }

    /**
     *
     * @return
     */
    public List<Prestamo> listarTodos() {
        return prestamoRepositorio.findAll();
    }

    /**
     *
     * @return
     */
    public List<Prestamo> listarDeAlta() {
        return prestamoRepositorio.buscarPrestamosAlta();
    }

    /**
     *
     * @return
     */
    public List<Prestamo> listarDeBaja() {
        return prestamoRepositorio.buscarPrestamosBaja();
    }

    /**
     *
     * @param idUsuario
     * @return
     */
    public List<Prestamo> listarDeAltaUsuario(String idUsuario) {
        return prestamoRepositorio.buscarPrestamosAltaUsuario(idUsuario);
    }

    /**
     *
     * @param idUsuario
     * @return
     */
    public List<Prestamo> listarDeBajaUsuario(String idUsuario) {
        return prestamoRepositorio.buscarPrestamosBajaUsuario(idUsuario);
    }

}
