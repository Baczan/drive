package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.entities.RegisterToken;
import com.baczan.session_authorization_server.helpers.EmailSender;
import com.baczan.session_authorization_server.helpers.PasswordValidator;
import com.baczan.session_authorization_server.helpers.ResponseHelper;
import com.baczan.session_authorization_server.repositories.RegisterTokenRepository;
import com.baczan.session_authorization_server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LoginController implements ErrorController {


    @Autowired
    private Environment environment;

    @GetMapping("/login")
    private String customLogin(@RequestParam Optional<String> redirectUrl, HttpServletRequest request){

        redirectUrl.ifPresent(s -> request.getSession().setAttribute("redirectUrl", s));
        return "login";
    }


    @GetMapping("/afterLogin")
    public String afterLogin(Authentication authentication, HttpServletResponse response, HttpServletRequest request, CsrfToken csrfToken){

        if(authentication!=null){
            ResponseHelper.setCookies(response,authentication,csrfToken);
        }

        String redirectUrl;

        if(request.getSession().getAttribute("redirectUrl") == null){
            redirectUrl = environment.getProperty("app.redirectUrl");
        }else{
            redirectUrl = request.getSession().getAttribute("redirectUrl").toString();
        }


        return "redirect:"+redirectUrl;
    }


    @RequestMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Delete authentication from context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        ResponseHelper.clearCookies(response);

        return "redirect:/login";
    }

    @RequestMapping("/error")
    public String error(){
        return "redirect:/login";
    }

}
