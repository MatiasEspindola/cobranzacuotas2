package com.proyectoCuotasRyR.proyectoCuotas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.proyectoCuotasRyR.proyectoCuotas.models.entities.Usuario;
import com.proyectoCuotasRyR.proyectoCuotas.models.repo.I_Usuario_Repo;
import com.proyectoCuotasRyR.proyectoCuotas.models.services.StorageService;
import com.proyectoCuotasRyR.proyectoCuotas.properties.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ProyectoCuotasApplication implements CommandLineRunner {

	@Autowired
	private I_Usuario_Repo repo;

	@Autowired
	private StorageService uploadFileService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private I_Usuario_Repo usuarioRepo;

	public static void main(String[] args) {
		SpringApplication.run(ProyectoCuotasApplication.class, args);
		
	}
	
	 @Bean
	    CommandLineRunner init(StorageService storageService)
	    {
	        return (args) ->
	        {
	        	storageService.init();
	            storageService.loadAll();
	        };
	    }

	public void run(String... args) throws Exception {
		
		
		
		
		
		
		// TODO Auto-generated method stub
		// uploadFileService.deleteAll();
		// uploadFileService.init();

		/*String password = "test"; 

		List<Usuario> usuarios = (List<Usuario>) repo.findAll();

		for (Usuario usuario : usuarios) {
			for (int i = 0; i < 2; i++) {
				String bcryptPassword = passwordEncoder.encode(password);
				System.out.println(bcryptPassword);
				usuario.setPassword(bcryptPassword);
				repo.save(usuario);
			}

		}
		
		*/

	}
	
	
	private Usuario obtenerUsuario() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		UserDetails userDetail = (UserDetails) auth.getPrincipal();

		System.out.println(userDetail.getUsername());

		return usuarioRepo.findByUsername(userDetail.getUsername());
	}

}
