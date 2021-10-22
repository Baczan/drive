package com.baczan.session_authorization_server.handlers;

import com.baczan.session_authorization_server.entities.Authority;
import com.baczan.session_authorization_server.entities.User;
import com.baczan.session_authorization_server.helpers.ResponseHelper;
import com.baczan.session_authorization_server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Oauth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {


        User user = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        OAuth2AuthenticationToken auth2AuthenticationToken = ((OAuth2AuthenticationToken) authentication);


        if (auth2AuthenticationToken
                .getAuthorizedClientRegistrationId()
                .equals("google")) {

            String googleId = ((OAuth2User) authentication.getPrincipal()).getAttribute("sub");
            String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");

            if (userRepository.existsByGoogleId(googleId)) {
                user = userRepository.getByGoogleId(googleId);

            } else if (userRepository.existsByEmail(email)) {

                User loadedUser = userRepository.getUserByEmail(email);
                loadedUser.setGoogleId(googleId);
                user = userRepository.save(loadedUser);

            } else {

                User newUser = new User();
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                newUser.setAuthorities(Collections.singletonList(new Authority("ROLE_USER", newUser)));

                user = userRepository.save(newUser);

            }

        } else {

            String facebookId = ((OAuth2User) authentication.getPrincipal()).getAttribute("id");
            String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");

            if (userRepository.existsByFacebookId(facebookId)) {
                user = userRepository.getByFacebookId(facebookId);

            } else if (userRepository.existsByEmail(email)) {
                User loadedUser = userRepository.getUserByEmail(email);
                loadedUser.setFacebookId(facebookId);
                user = userRepository.save(loadedUser);
            } else {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFacebookId(facebookId);
                newUser.setAuthorities(Collections.singletonList(new Authority("ROLE_USER", newUser)));

                user = userRepository.save(newUser);

            }
        }




        List<GrantedAuthority> authorities = user.getAuthorities().stream().map(authority -> new SimpleGrantedAuthority(authority.getRole())).collect(Collectors.toList());


        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")
                .authorities(authorities)
                .build();


        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(userDetails,null,authorities);



        securityContext.setAuthentication(newAuthentication);



        response.sendRedirect("http://localhost:8080/afterLogin");
    }
}


