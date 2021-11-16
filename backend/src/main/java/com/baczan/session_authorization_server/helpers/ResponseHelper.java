package com.baczan.session_authorization_server.helpers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;

public class ResponseHelper {

    public static void setCookies(HttpServletResponse response, Authentication authentication, CsrfToken csrfToken,String domain){

        int expiryDate = 60*60*24*365;

        String authoritiesBase64 = Base64.getEncoder().encodeToString(authentication.getAuthorities().toString().getBytes());

        Cookie cookie = new Cookie("authorities", authoritiesBase64);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(expiryDate);
        response.addCookie(cookie);

        cookie = new Cookie("email",authentication.getName());
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(expiryDate);
        response.addCookie(cookie);

        cookie = new Cookie("csrf",csrfToken.getToken());
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(expiryDate);
        response.addCookie(cookie);

    }

    public static void clearCookies(HttpServletResponse response,String domain){


        Cookie cookie = new Cookie("authorities","");
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        cookie = new Cookie("email","");
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        cookie = new Cookie("csrf","");
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


}
