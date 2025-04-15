package mx.edu.utez.sacit.security;

import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserLogin  implements UserDetailsService {
    private  final UserService service;

    public UserLogin(UserService service){
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = this.service.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username " + username + " no existe en el sistema");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().getRole()));
        return  new User(user.getEmail(), user.getPassword(), true, true, true, true, authorities);
    }
}
