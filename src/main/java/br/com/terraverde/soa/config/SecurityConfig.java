package br.com.terraverde.soa.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.com.terraverde.soa.model.Usuario;
import br.com.terraverde.soa.repository.UsuarioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    // Define o PasswordEncoder a ser usado em toda a aplicação (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF para facilitar o uso da API REST
            .csrf(AbstractHttpConfigurer::disable)
            // Permite todas as requisições a URLs públicas e estáticas
            .authorizeHttpRequests(authorize -> authorize
                // Permite acesso irrestrito, focando a segurança na API de login
                .anyRequest().permitAll() 
            );
        return http.build();
    }

    // Configuração de CORS (Cross-Origin Resource Sharing)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos os endpoints
            .allowedOrigins("*") // Permite acesso de qualquer origem (em desenvolvimento)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite métodos HTTP comuns
            .allowedHeaders("*");
    }

    /**
     * Componente para criar um usuário administrador inicial se não houver nenhum.
     * Isso garante que você possa logar na primeira execução.
     */
    @Bean
    public CommandLineRunner initUser(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                // Senha inicial: 'admin123' (criptografada)
                admin.setPassword(passwordEncoder.encode("admin123")); 
                repository.save(admin);
                System.out.println(">>> Usuário ADMIN inicial criado: admin/admin123");
            }
        };
    }
}


