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
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/error").permitAll()




                        // PRIVATE PAGES
                        .requestMatchers("/followProduct").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/product/{id}/newComment").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/removeProductFromCart").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/shoppingCart").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/product/{id}/comment/{CID}").hasAnyRole("USER","ADMIN")

                        .requestMatchers("/newProduct").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/delete").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/edit").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/modify").hasRole("ADMIN")

                        .requestMatchers("/profile").hasAnyRole("USER","ADMIN")

                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/loginerror")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Override
    public void customize(WebSecurity web) {
        web.ignoring().requestMatchers("/css/**");
        web.ignoring().requestMatchers("/js/**");
        web.ignoring().requestMatchers("/images/**");
    }
}

