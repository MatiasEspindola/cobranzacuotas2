package com.proyectoCuotasRyR.proyectoCuotas.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Empresa;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Localidad;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Provincia;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Sucursal;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Usuario_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Empresa_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_GeoService;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Plan_Pago_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Responsable_Iva_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Sucursal_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.StorageService;

@Controller
@SessionAttributes("empresa")
@RequestMapping("/empresas")
public class empresaController {

	@Autowired
	private I_Empresa_Service empresaService;

	@Autowired
	private I_Sucursal_Service sucursalService;

	@Autowired
	private I_Responsable_Iva_Service responsableIvaService;

	@Autowired
	private I_Usuario_Repo usuarioRepo;

	@Autowired
	private I_GeoService geoService;

	private final StorageService storageService;

    @Autowired
    public empresaController(StorageService storageService)
    {
        this.storageService = storageService;
    }
	
	@Autowired
	private I_Plan_Pago_Service planPagoService;

	private boolean editar;

	@GetMapping(value = "/provincias", produces = { "application/json" })
	public @ResponseBody List<Provincia> provincias() {
		return geoService.provincias();
	}

	@GetMapping(value = "/localidades", produces = { "application/json" })
	public @ResponseBody List<Localidad> localidades(@RequestParam String id_provincia) {
		return geoService.buscarPorId(Integer.valueOf(id_provincia)).getLocalidades();
	}

	 @GetMapping("/files/{filename:.+}")
	    @ResponseBody
	    public ResponseEntity<Resource> serveFile(@PathVariable String filename)
	    {
	        Resource file = storageService.loadAsResource(filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=\""+file.getFilename()+"\"").body(file);
	    }

	@PreAuthorize("hasAuthority('Admin')")
	@GetMapping("/registrar")
	public String formulario(Model model) {

		editar = false;

		Empresa empresa = new Empresa();

		model.addAttribute("responsables_iva", responsableIvaService.listar_todo());

		model.addAttribute("empresa", empresa);

		model.addAttribute("usuario", obtenerUsuario());
		
		model.addAttribute("notificaciones", planPagoService.listarTodo());

		return "empresas/registrar";
	}

	@PreAuthorize("hasAuthority('Admin')")
	@GetMapping("/registrar/{id_empresa}")
	public String formulario(Model model, @PathVariable long id_empresa, RedirectAttributes redirectAttrs) {

		editar = true;
		
		if(empresaService.buscarPorId(id_empresa) == null) {
			redirectAttrs.addFlashAttribute("error", "Empresa con ese ID no existe");
			return "redirect:/";
		}

		if (empresaService.listar_todo().size() == 0) {
			redirectAttrs.addFlashAttribute("error", "No hay ninguna empresa registrada");
			return "redirect:/";
		}

		Empresa empresa = empresaService.buscarPorId(id_empresa);

		model.addAttribute("responsables_iva", responsableIvaService.listar_todo());

		model.addAttribute("empresa", empresa);

		model.addAttribute("usuario", obtenerUsuario());
		
		model.addAttribute("notificaciones", planPagoService.listarTodo());

		return "empresas/registrar";
	}

	@PreAuthorize("hasAuthority('Admin')")
	@PostMapping("/registrar")
	public String guardar(@Valid Empresa empresa,			RedirectAttributes redirectAttrs) {

		if (empresaService.listar_todo().size() > 0 && !editar) {
			redirectAttrs.addFlashAttribute("error", "Sólo puede registrar una empresa");

			return "redirect:/";
		}
		
	

		

		redirectAttrs.addFlashAttribute("success", "Los datos se han guardado éxito");

		empresaService.guardar(empresa);

		return "redirect:/";
	}

	private Usuario obtenerUsuario() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		UserDetails userDetail = (UserDetails) auth.getPrincipal();

		System.out.println(userDetail.getUsername());

		return usuarioRepo.findByUsername(userDetail.getUsername());
	}

}
