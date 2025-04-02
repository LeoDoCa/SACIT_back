package mx.edu.utez.SACIT.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository repository;
    public JwtTokenFilter(JwtTokenUtil jwtTokenUtil, UserRepository repository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!hasAuthorizationBearer(request)){
            filterChain.doFilter(request, response);
            return;
        }
        String token = getAccessToken(request);
        if (!jwtTokenUtil.validateAccessToken(token)){
            filterChain.doFilter(request, response);
            return;
        }
        setAuthenticationContext(token, request);
        filterChain.doFilter(request, response);
    }

    private boolean hasAuthorizationBearer(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        return !ObjectUtils.isEmpty(header) && header.startsWith("Bearer");
    }

    private String getAccessToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        return header.split(" ")[1].trim();
    }

private void setAuthenticationContext(String token, HttpServletRequest request) {
    UserModel userDetails = getUserDetails(token);
    List<GrantedAuthority> authorities = Collections.singletonList(
        new SimpleGrantedAuthority(userDetails.getRole().getRole())
    );

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
}

private UserModel getUserDetails(String token) {

    String[] jwtSubject = jwtTokenUtil.getSubject(token).split(",");
    Optional<UserModel> userDetails = repository.findById(Integer.parseInt(jwtSubject[0]));
    return userDetails.orElse(null);
}
}
