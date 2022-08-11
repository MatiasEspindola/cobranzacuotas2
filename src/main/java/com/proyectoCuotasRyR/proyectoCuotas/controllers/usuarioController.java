package com.proyectoCuotasRyR.proyectoCuotas.controllers;

import java.net.MalformedURLException;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Authority;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Rol;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Sucursal;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario_Sucursal;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Usuario_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Authority_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Empresa_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Plan_Pago_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Rol_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Sucursal_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.StorageService;

@Controller
@SessionAttributes("usuario")
@RequestMapping("/usuarios")
public class usuarioController {
	
	@Autowired
	private I_Empresa_Service empresaService;
	
	@Autowired
	private I_Usuario_Repo usuarioRepo;
	
		
	@Autowired
	private I_Sucursal_Service sucursalService;
	
	@Autowired
	private I_Plan_Pago_Service planPagoService;
	
	@Autowired
	private I_Rol_Service rolService;
	
	@Autowired
	private I_Authority_Service authorityService;
	
	private final StorageService storageService;

    @Autowired
    public usuarioController(StorageService storageService)
    {
        this.storageService = storageService;
    }
    
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename)
    {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=\""+file.getFilename()+"\"").body(file);
    }
	
	@PreAuthorize("hasAuthority('Admin')")
	@GetMapping("/listar")
	public String listar(Model model, RedirectAttributes redirectAttrs) {
		
	
		if (empresaService.listar_todo().size() == 0) {
			redirectAttrs.addFlashAttribute("error", "Debe primero registrar una empresa");
			return "redirect:/";
		}
		
		model.addAttribute("empresa", empresaService.listar_todo().get(0));
		model.addAttribute("usuario", obtenerUsuario());
		model.addAttribute("usuarios", usuarioRepo.findAll());
		
		model.addAttribute("roles", rolService.listarTodo());
		
		model.addAttribute("notificaciones", planPagoService.listarTodo());
		
		return "usuarios/listar";
	}
	
	@PreAuthorize("hasAuthority('Admin')")
	@PostMapping("/asignar_rol")
	public String asignar_rol(@RequestParam(name="rol") Rol rol, @RequestParam(name="usuario") Usuario usuario, RedirectAttributes redirectAttrs) {

		if (empresaService.listar_todo().size() == 0) {
			redirectAttrs.addFlashAttribute("error", "Debe primero registrar una empresa");
			return "redirect:/";
		}
	
		usuario.getAuthorities().get(0).setId_rol_auth(rol);
		usuarioRepo.save(usuario);
		

		return "redirect:/";

	}
	
	@PreAuthorize("hasAuthority('Admin')")
	@GetMapping("/deshabilitar/{id_usuario}")
	public String listar(Model model, @PathVariable long id_usuario, RedirectAttributes redirectAttrs) {
		
		if (empresaService.listar_todo().size() == 0) {
			redirectAttrs.addFlashAttribute("error", "Debe primero registrar una empresa");
			return "redirect:/";
		}
		
		if(usuarioRepo.findById(id_usuario) == null) {
			redirectAttrs.addFlashAttribute("error", "No existe usuario con ese ID");
			return "redirect:/";
		}
	
		model.addAttribute("notificaciones", planPagoService.listarTodo());
		
		model.addAttribute("empresa", empresaService.listar_todo().get(0));
		
		
		if(usuarioRepo.findById(id_usuario).get().isActivo()) {
			usuarioRepo.findById(id_usuario).get().setActivo(false);
		}else {
			usuarioRepo.findById(id_usuario).get().setActivo(true);
		}
		
		usuarioRepo.save(usuarioRepo.findById(id_usuario).get());
		
		
		return "redirect:/usuarios/listar";
	}
	
	@PreAuthorize("hasAuthority('Admin')")
	@GetMapping("/cambiar_rol/{id_usuario}")
	public String cambiar_rol(Model model, @PathVariable long id_usuario, RedirectAttributes redirectAttrs) {
		
		if (empresaService.listar_todo().size() == 0) {
			redirectAttrs.addFlashAttribute("error", "Debe primero registrar una empresa");
			return "redirect:/";
		}
		
		if(usuarioRepo.findById(id_usuario) == null) {
			redirectAttrs.addFlashAttribute("error", "No existe usuario con ese ID");
			return "redirect:/";
		}
	
		Usuario usuario = usuarioRepo.findById(id_usuario).get();
		
		
		if(usuario.getAuthorities().get(0).getId_rol_auth().getId_rol() == 1) {
			Authority authority = authorityService.buscarPorUsuario(usuario);
			authority.setId_rol_auth(rolService.buscarPorId(2));
			authorityService.guardar(authority);
		} else {
			Authority authority = authorityService.buscarPorUsuario(usuario);
			authority.setId_rol_auth(rolService.buscarPorId(1));
			authorityService.guardar(authority);
		}
		
	
		
		
		return "redirect:/usuarios/listar";
	}
	
	
	
	private Usuario obtenerUsuario() {

		Authentication auth = SecurityContextHolder
		            .getContext()
		            .getAuthentication();
		   
		UserDetails userDetail = (UserDetails) auth.getPrincipal();
  
		System.out.println(userDetail.getUsername());
		
		return usuarioRepo.findByUsername(userDetail.getUsername());
	}
	


}
