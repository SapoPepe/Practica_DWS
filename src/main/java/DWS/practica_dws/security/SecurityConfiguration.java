package DWS.practica_dws.security;

import DWS.practica_dws.service.RepositoryUserDetailsService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration implements WebSecurityCustomizer {
    @Autowired
    public RepositoryUserDetailsService userDetailService;

    @Value("${jwt.public.key}")
    RSAPublicKey key;
    @Value("${jwt.private.key}")
    RSAPrivateKey priv;
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }
    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
    static class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
        public AbstractAuthenticationToken convert(Jwt jwt) {
            Collection<String> authorities = jwt.getClaimAsStringList("scope");
            Collection<GrantedAuthority> grantedAuthorities = authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new JwtAuthenticationToken(jwt, grantedAuthorities);
        }
    }

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
    public SecurityFilterChain securityJwtTokeFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/token")
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/token").authenticated()
                )
                .csrf((csrf) -> csrf.ignoringRequestMatchers("/api/token"))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public SecurityFilterChain securityJwtFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/user/shoppingCart").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/user/shoppingCart").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user/shoppingCart").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products/*/comments").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*/comments/*").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/persons/*").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/persons/*").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/persons/*").hasAnyRole("USER","ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products/*/image").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*/image").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/*/image").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products/*/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/*/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/persons").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/file").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/persons/register").permitAll()
                )
                .csrf((csrf) -> csrf.ignoringRequestMatchers("/api/products"))
                .oauth2ResourceServer(oAuth -> oAuth.jwt(Customizer.withDefaults()))
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(new CustomAuthenticationConverter())
                )
        );
        return http.build();
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

                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/newProduct").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/delete").hasRole("ADMIN")
                        .requestMatchers("/deletePerson/{id}").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/edit").hasRole("ADMIN")
                        .requestMatchers("/product/{id}/modify").hasRole("ADMIN")
                        .requestMatchers("/adminPannel").hasRole("ADMIN")

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

