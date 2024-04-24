package DWS.practica_dws.security;

import DWS.practica_dws.service.RepositoryUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration implements WebSecurityCustomizer {
    @Autowired
    public RepositoryUserDetailsService userDetailService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(authorize -> authorize
                        // PUBLIC PAGES  -- Faltan incluir las de la API REST
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/product/{id}").permitAll()
                        .requestMatchers("/product/{id}/file").permitAll()
                        .requestMatchers("/product/{id}/image").permitAll()
                        .requestMatchers("/searchProduct").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/loginerror").permitAll()
                        .requestMatchers("/error").permitAll()




                        // PRIVATE PAGES
                        .requestMatchers("/followProduct").hasAnyRole("USER")
                        .requestMatchers("/product/{id}/newComment").hasAnyRole("USER")
                        .requestMatchers("/removeProductFromCart").hasAnyRole("USER")
                        .requestMatchers("/shoppingCart").hasAnyRole("USER")

                        .requestMatchers("/newProduct").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/comment/{CID}").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/delete").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/edit").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/modify").hasRole("ADMIN")

                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/loginerror")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        // Disable CSRF at the moment
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Override
    public void customize(WebSecurity web) {
        web.ignoring().requestMatchers("/css/**");
        web.ignoring().requestMatchers("/js/**");
        web.ignoring().requestMatchers("/images/**");
    }
}

