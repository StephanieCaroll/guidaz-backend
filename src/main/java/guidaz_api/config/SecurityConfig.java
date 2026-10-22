package guidaz_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desativa CSRF para testes via Postman/Thunder Client
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Libera todas as rotas sem precisar de autenticação
            );
        return http.build();
    }
}