package com.proyectoCuotasRyR.proyectoCuotas.controllers;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import com.proyectoCuotasRyR.proyectoCuotas.exception.StorageFileNotFoundException;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Cuota;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Empresa;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Plan_Pago;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Sucursal;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario;
import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario_Sucursal;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Usuario_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Usuario_Sucursal_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Cuota_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Empresa_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Plan_Pago_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.I_Sucursal_Service;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.StorageService;

@Controller
public class indexController {

	@Autowired
	private I_Usuario_Repo usuarioRepo;

	@Autowired
	private I_Empresa_Service empresaService;

	@Autowired
	private I_Sucursal_Service sucursalService;

	@Autowired
	private I_Plan_Pago_Service planPagoService;

	@Autowired
	private I_Cuota_Service cuotaService;

	private final StorageService storageService;

	@Autowired
	public indexController(StorageService storageService) {
		this.storageService = storageService;
	}


	@GetMapping("/uploads/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@GetMapping("/inactivo")
	public String inactivo() {

		return "inactivo";
	}

	@GetMapping({ "/", "/index" })
	public String index(Model model, @RequestParam(value = "logout", required = false) String logout,
			RedirectAttributes redirectAttrs) {

		if (obtenerUsuario() == null) {

			return "redirect:/logout";
		}

		if (!obtenerUsuario().isActivo()) {
			return "redirect:/inactivo";
		}

		Empresa empresa = null;

		if (empresaService.listar_todo().size() == 0) {

			return "redirect:/empresas/registrar";
		}

		if (empresaService.listar_todo().size() > 0) {
			empresa = empresaService.listar_todo().get(0);
		}
		
		 model.addAttribute("files",storageService.loadAll().map(
	                path -> MvcUriComponentsBuilder.fromMethodName(indexController.class,
	                "serveFile",path.getFileName().toString()).build().toString())
	                .collect(Collectors.toList()));

		model.addAttribute("empresa", empresa);
		model.addAttribute("usuario", obtenerUsuario());

		model.addAttribute("notificaciones", planPagoService.listarTodo());

		return "index";
	}

	@PostMapping("/index")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		Empresa empresa = empresaService.listar_todo().get(0);
		empresa.setLogo(file.getOriginalFilename());

		empresaService.guardar(empresa);
		storageService.store(file);
		redirectAttributes.addFlashAttribute("message", "You successully uploaded " + file.getOriginalFilename() + "!");
		return "redirect:/index";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException e) {
		return ResponseEntity.notFound().build();
	}



	private Usuario obtenerUsuario() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		UserDetails userDetail = (UserDetails) auth.getPrincipal();

		System.out.println(userDetail.getUsername());

		return usuarioRepo.findByUsername(userDetail.getUsername());
	}

}
