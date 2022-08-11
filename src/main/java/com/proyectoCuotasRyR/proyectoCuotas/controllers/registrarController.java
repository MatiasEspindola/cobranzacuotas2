package com.proyectoCuotasRyR.proyectoCuotas.controllers;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Authority;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Localidad;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Medio_Pago;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Provincia;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Rol;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Authority_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Rol_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Usuario_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Authority_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_GeoService;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.StorageService;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.PreguntaService;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.ResponsableService;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.RolService;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.UsuarioService;

@Controller
@SessionAttributes("usuario")
public class registrarController {

	@Autowired
	private I_Usuario_Repo usuarioRepo;

	@Autowired
	private PreguntaService preguntaService;

	@Autowired
	private ResponsableService responsableService;

	@Autowired
	private StorageService upl;

	@Autowired
	private I_GeoService geoService;

	@Autowired
	private I_Authority_Service authorityService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private RolService rolService;

	@GetMapping(value = "/provincias", produces = { "application/json" })
	public @ResponseBody List<Provincia> provincias() {
		return geoService.provincias();
	}

	@GetMapping(value = "/localidades", produces = { "application/json" })
	public @ResponseBody List<Localidad> localidades(@RequestParam String id_provincia) {
		return geoService.buscarPorId(Integer.valueOf(id_provincia)).getLocalidades();
	}

	@GetMapping("/registrar")
	public String registrar(Model model) {

		model.addAttribute("usuario", new Usuario());
		model.addAttribute("preguntas", preguntaService.listar());
		model.addAttribute("responsables_iva", responsableService.listar_todo());

		return "registrar";
	}

	@PostMapping("/registrar")
	public String guardar(@Valid Usuario usuario, Model model) {
		
		for(Usuario user : usuarioRepo.findAll()) {
			if(user.getUsername() == usuario.getUsername()) {
				return "registrar";
			}
		}
		
		String bcryptPassword = passwordEncoder.encode(usuario.getPassword());
		System.out.println(bcryptPassword);

		usuario.setPass2(usuario.getPassword());

		usuario.setPassword(bcryptPassword);
		usuario.setFecha_alta(new Date());
		usuario.setActivo(false);
		usuarioRepo.save(usuario);

		Authority authority = new Authority();

		authority.setId_rol_auth(rolService.buscarPorId(2));
		authority.setId_usuario_auth(usuario);

		authorityService.guardar(authority);
		
		System.out.println("Se ha creado la cuenta " + usuario.getUsername() + ", con el ROL: " + authority.getId_rol_auth().getRol());

		return "redirect:/login";

	}

}
