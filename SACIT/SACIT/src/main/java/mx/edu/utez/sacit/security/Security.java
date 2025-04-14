package mx.edu.utez.sacit.security;

import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sacit.jwt.JwtTokenFilter;
import mx.edu.utez.sacit.service.AccessLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class Security {
    private final JwtTokenFilter jwtTokenFilter;

    public Security(JwtTokenFilter jwtTokenFilter, AccessLogService service) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.service = service;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    String ADMIN = "ROLE_ADMIN";
    String WINDOW = "ROLE_WINDOW";
    String USER = "ROLE_USER";
    private final String[] ADMIN_LIST = {
            "/api/user/**",
            "/api/window/**",
            "/api/appointments/**",
            "/api/appointments/today",
            "/api/procedures/**",
            "/api/required-documents/",
    };

    private final String[] WINDOW_LIST = {
            "/api/window/{windowUuid}/appointments",
            "/api/appointments/{appointmentUuid}/documents",
            "/api/appointments/{uuid}/status",
    };

    private final String[] USER_LIST = {
            "/api/appointments/user/{userUuid}"
    };

    private final AccessLogService service;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) -> {
                            String ip = request.getRemoteAddr();
                            String resource = request.getRequestURI();
                            service.registerEvent("ANÓNIMO", "ACCESO_DENEGADO", ip, resource);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/login", "/api/send-otp", "/api/verify-otp", "/api/validate-credentials", "/api/register", "/api/recover-password-email", "/api/reset-password/{token}", "/api/validate-token/**","/api/procedures/","api/procedures/{uuid}","/api/required-documents/","/api/required-documents/{uuid}", "/api/appointments/")
                            .permitAll();

                    for(String route : USER_LIST) {
                        auth.requestMatchers(route).hasAuthority(USER);
                    }

                    for(String route : WINDOW_LIST) {
                        auth.requestMatchers(route).hasAuthority(WINDOW);
                    }


                    for (String route : ADMIN_LIST) {
                        auth.requestMatchers(route).hasAuthority(ADMIN);
                    }

                    auth.anyRequest().authenticated();
                });

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
