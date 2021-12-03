package com.egg.libreriaEgg.controllers;

import com.egg.libreriaEgg.entidades.Editorial;
import com.egg.libreriaEgg.repositorios.EditorialRepositorio;
import com.egg.libreriaEgg.servicios.EditorialServicio;
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

/**
 * Controlador para gestionar la entidad Editorial (listar, registrar,
 * modificar, dar de alta/baja, eliminar).
 *
 * @author Mauro Montenegro <maumontenegro.s at gmail.com>
 */
@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin/editoriales")
public class EditorialController {

    @Autowired
    private EditorialRepositorio editorialRepositorio;

    @Autowired
    private EditorialServicio editorialServicio;

    /**
     * Muestra el Menú Administrativo de Editoriales, con los datos de todos las
     * editoriales inyectadas al modelo.
     *
     * @param model
     * @return
     */
    @GetMapping("/admin-editoriales")
    public String administradorEditoriales(ModelMap model) {
        List<Editorial> editoriales = editorialRepositorio.findAll();
        model.put("editoriales", editoriales);
        return "admin-editorial.html";
    }

    /**
     * Función para registrar una editorial. Una vez seteados los atributos
     * desde el servicio, muestra la página de "admin-editorial.html" con
     * mensajes inyectados al modelo.
     *
     * @param model
     * @param id
     * @param nombre
     * @return
     */
    @PostMapping("/registrar-editorial")
    public String registrarEditorial(ModelMap model, @RequestParam(required = false) String id, @RequestParam String nombre) {

        try {
            editorialServicio.agregarEditorial(nombre);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "La editorial '" + nombre.toUpperCase() + "' fue registrada exitosamente.");
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo de "error.html":
            if (e.getMessage() == null || nombre == null) {
                model.put("error", "Error al intentar guardar la editorial: faltó completar algún campo.");
            } else {
                model.put("error", "Error al intentar guardar la editorial: " + e.getMessage());
            }
        }
        List<Editorial> editoriales = editorialRepositorio.findAll();
        model.put("editoriales", editoriales);
        return "admin-editorial.html";
    }

    /**
     * Función que carga la vista para modificar los datos de una editorial
     * elegida previamente. Busca la editorial en el repositorio por id, y la
     * inyecta al modelo para tener todos sus datos. Es una url con "path
     * variable" (id de la editorial a modificar).
     *
     * @param model
     * @param idEditorialModif
     * @return
     */
    @GetMapping("/modificar-editorial-datos/{idEditorialModif}")
    public String datosEditorial(ModelMap model, @PathVariable String idEditorialModif) {
        Editorial editorial = editorialRepositorio.getById(idEditorialModif);
        model.put("editorialModif", editorial);
        return "modif-editorial.html";
    }

    /**
     * Función para modificar una editorial.
     *
     * ESTE MÉTODO USA EL MODEL PARA QUE APAREZCA LA ALERTA ("success") EN LA
     * MISMA PLANTILA DE "admin-editorial.html", O LA ALERTA ("error") EN LA
     * PLANTILLA DE "modif-editorial.html".
     *
     * @param model
     * @param id
     * @param nombre
     * @return
     */
    @PostMapping("/modificar-editorial")
    public String modificarEditorial(ModelMap model, @RequestParam String id, @RequestParam String nombre) {

        try {
            editorialServicio.modificarEditorial(id, nombre);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "La editorial '" + nombre.toUpperCase() + "' fue modificada exitosamente.");
            List<Editorial> editoriales = editorialRepositorio.findAll();
            model.put("editoriales", editoriales);
            return "admin-editorial.html";
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo:
            Editorial editorial = editorialRepositorio.getById(id);
            model.put("editorialModif", editorial);
            model.put("error", "Error al intentar modificar la editorial: " + e.getMessage());
            return "modif-editorial.html";
        }
    }

    /**
     * Función para eliminar una editorial. Antes de eliminarla desde el
     * servicio, capturo el nombre en una variable para poder utilizarlo en la
     * página "admin-editorial" con mensajes inyectados al modelo. Es una url
     * con "path variable" (id de la editorial a eliminar).
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/eliminar-editorial/{id}")
    public String eliminarEditorial(ModelMap model, @PathVariable String id) {
        try {
            String nombre = editorialRepositorio.getById(id).getNombre().toUpperCase();
            // Con el id, llamo al método para eliminar la editorial:
            editorialServicio.eliminarEditorial(id);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "La editorial '" + nombre + "' fue eliminada exitosamente.");
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo:
            model.put("error", "Error al intentar eliminar la editorial: " + e.getMessage());
        }
        List<Editorial> editoriales = editorialRepositorio.findAll();
        model.put("editoriales", editoriales);
        return "admin-editorial.html";
    }

    /**
     * Función para dar de baja una editorial. Una vez modificado el atributo
     * "alta" desde el servicio, muestra la página de "admin-editorial" con
     * mensajes inyectados al modelo. Es una url con "path variable" (id de la
     * editorial a dar de baja).
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/baja/{id}")
    public String baja(ModelMap model, @PathVariable String id) {
        try {
            editorialServicio.baja(id);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "La editorial '" + editorialRepositorio.getOne(id).getNombre().toUpperCase() + "' fue dada de baja exitosamente.");
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo:
            model.put("error", "Error al intentar dar de baja la editorial: " + e.getMessage());
        }
        List<Editorial> editoriales = editorialRepositorio.findAll();
        model.put("editoriales", editoriales);
        return "admin-editorial.html";
    }

    /**
     * Función para dar de alta una editorial. Una vez modificado el atributo
     * "alta" desde el servicio, muestra la página de "admin-editorial" con
     * mensajes inyectados al modelo. Es una url con "path variable" (id de la
     * editorial a dar de alta).
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("/alta/{id}")
    public String alta(ModelMap model, @PathVariable String id) {
        try {
            editorialServicio.alta(id);
            // Mensaje de éxito inyectado al modelo:
            model.put("success", "La editorial '" + editorialRepositorio.getById(id).getNombre().toUpperCase() + "' fue dada de alta exitosamente.");
        } catch (Exception e) {
            // Mensaje de error inyectado al modelo:
            model.put("error", "Error al intentar dar de alta la editorial: " + e.getMessage());
        }
        List<Editorial> editoriales = editorialRepositorio.findAll();
        model.put("editoriales", editoriales);
        return "admin-editorial.html";
    }
}
