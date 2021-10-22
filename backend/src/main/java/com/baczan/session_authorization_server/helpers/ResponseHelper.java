package com.baczan.session_authorization_server.helpers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;

public class ResponseHelper {

    public static void setCookies(HttpServletResponse response, Authentication authentication, CsrfToken csrfToken){

        int expiryDate = 60*60*24*365;

        String authoritiesBase64 = Base64.getEncoder().encodeToString(authentication.getAuthorities().toString().getBytes());

        Cookie cookie = new Cookie("authorities", authoritiesBase64);
        cookie.setMaxAge(expiryDate);
        response.addCookie(cookie);

        cookie = new Cookie("email",authentication.getName());
        cookie.setMaxAge(expiryDate);
        response.addCookie(cookie);

        cookie = new Cookie("csrf",csrfToken.getToken());
        cookie.setMaxAge(expiryDate);
        response.addCookie(cookie);

    }

    public static void clearCookies(HttpServletResponse response){


        Cookie cookie = new Cookie("authorities","");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        cookie = new Cookie("email","");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        cookie = new Cookie("csrf","");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


}
