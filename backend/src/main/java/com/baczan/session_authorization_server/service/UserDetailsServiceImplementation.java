package com.baczan.session_authorization_server.service;

import com.baczan.session_authorization_server.entities.User;
import com.baczan.session_authorization_server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.stream.Collectors;

public class UserDetailsServiceImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        Optional<User> userOptional = userRepository.findUserByEmailAndEmailActivatedIsTrue(s);

        if(userOptional.isEmpty()){
            throw new UsernameNotFoundException(s);
        }

        User user = userOptional.get();

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getAuthorities().stream().map(authority -> new SimpleGrantedAuthority(authority.getRole())).collect(Collectors.toList()))
                .build();
    }
}
