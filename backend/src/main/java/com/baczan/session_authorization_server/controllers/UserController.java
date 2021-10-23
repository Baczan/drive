package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.entities.Authority;
import com.baczan.session_authorization_server.entities.PasswordChangeToken;
import com.baczan.session_authorization_server.entities.RegisterToken;
import com.baczan.session_authorization_server.entities.User;
import com.baczan.session_authorization_server.helpers.EmailSender;
import com.baczan.session_authorization_server.helpers.PasswordValidator;
import com.baczan.session_authorization_server.repositories.PasswordChangeTokenRepository;
import com.baczan.session_authorization_server.repositories.RegisterTokenRepository;
import com.baczan.session_authorization_server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private RegisterTokenRepository registerTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordChangeTokenRepository passwordChangeTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired private EmailSender emailSender;

    @GetMapping("/activate")
    @Transactional
    public String activation(@RequestParam String id, Model model) {


        UUID tokenId;
        try {
            tokenId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error","uuidError");
            return "activation";
        }

        Optional<RegisterToken> registerTokenOptional = registerTokenRepository.findById(tokenId);

        if(registerTokenOptional.isEmpty()){
            model.addAttribute("error","empty");
            return "activation";
        }

        RegisterToken registerToken = registerTokenOptional.get();

        if (userRepository.existsByEmailAndEmailActivatedIsTrue(registerToken.getEmail())) {
            model.addAttribute("error","alreadyExist");
            return "activation";
        }

        if (userRepository.existsByEmail(registerToken.getEmail())) {

            User user = userRepository.getUserByEmail(registerToken.getEmail());
            user.setPassword(registerToken.getPassword());
            user.setEmailActivated(true);
            userRepository.save(user);

        } else {

            User user = new User();
            user.setEmail(registerToken.getEmail());
            user.setPassword(registerToken.getPassword());
            user.setEmailActivated(true);
            user.setAuthorities(Collections.singletonList(new Authority("ROLE_USER", user)));

            userRepository.save(user);
        }

        registerTokenRepository.deleteAllByEmail(registerToken.getEmail());
        return "activation";
    }

    @RequestMapping(path = "/register",method= RequestMethod.GET)
    public String register(){
        return "registration";
    }

    @RequestMapping(path = "/register",method= RequestMethod.POST)
    public String registerPOST(Model model, @RequestParam String username,@RequestParam String password,@RequestParam String passwordC) throws MessagingException {


        if(username.trim().equals("")){
            model.addAttribute("errorUsername","usernameEmpty");
        }

        if(password.trim().equals("")){
            model.addAttribute("errorPassword","passwordEmpty");
        }

        if(passwordC.trim().equals("")){
            model.addAttribute("errorPasswordC","passwordCEmpty");
        }

        if(username.trim().equals("") || password.trim().equals("") || passwordC.trim().equals("")){
            return redirectToRegister(username,password,passwordC,model);
        }

        if (userRepository.existsByEmailAndEmailActivatedIsTrue(username)) {

            model.addAttribute("errorUsername","usernameAlreadyExists");
            return redirectToRegister(username,password,passwordC,model);
        }

        if (!PasswordValidator.validatePassword(password)) {
            model.addAttribute("errorPassword","validation");
            return redirectToRegister(username,password,passwordC,model);
        }

        if(!password.equals(passwordC)){
            model.addAttribute("errorPasswordC","match");
            return redirectToRegister(username,password,passwordC,model);
        }

        RegisterToken registerToken =
                new RegisterToken(UUID.randomUUID(), username, passwordEncoder.encode(password));

        registerTokenRepository.save(registerToken);

        emailSender.sendRegistrationEmail(registerToken);


        return "registrationSuccess";
    }

    private String redirectToRegister(String username,String password,String passwordC,Model model){

        model.addAttribute("username",username);
        model.addAttribute("password",password);
        model.addAttribute("passwordC",passwordC);

        return "registration";
    }

    @GetMapping("/password_change_request")
    public String passwordRecoveryRequest(){
        return "password_change_request";
    }

    @PostMapping("/password_change_request")
    public String passwordRecoveryRequestPost(@RequestParam String email,Model model)
            throws MessagingException {

        if (!userRepository.existsByEmailAndEmailActivatedIsTrue(email)) {
            model.addAttribute("error","notfound");
            return "password_change_request";
        }

        PasswordChangeToken passwordChangeToken = new PasswordChangeToken(UUID.randomUUID(), email);
        passwordChangeTokenRepository.save(passwordChangeToken);

        emailSender.sendPasswordChangeToken(passwordChangeToken);

        return "password_change_request_success";
    }

    @GetMapping("/password_change")
    public String passwordRecovery(@RequestParam String token,Model model){

        model.addAttribute("token",token);
        return "password_change";
    }

    @PostMapping("/password_change")
    @Transactional
    public String passwordRecoveryPost(
            @RequestParam String token, @RequestParam String password,@RequestParam String passwordC,Model model){


        model.addAttribute("password",password);
        model.addAttribute("passwordC",passwordC);

        UUID tokenId;
        try {
            tokenId = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorToken","tokenError");
            return "password_change_message";
        }

        Optional<PasswordChangeToken> passwordChangeTokenOptional = passwordChangeTokenRepository.findById(tokenId);

        if(passwordChangeTokenOptional.isEmpty()){
            model.addAttribute("errorToken","tokenNotFound");
            return "password_change_message";
        }

        PasswordChangeToken passwordChangeToken = passwordChangeTokenOptional.get();


        if (!PasswordValidator.validatePassword(password)) {
            model.addAttribute("errorPassword","invalid");
        }

        if(password.trim()==""){
            model.addAttribute("errorPassword","empty");
        }

        if(passwordC.trim()==""){
            model.addAttribute("errorPasswordC","empty");
        }



        if(password.trim()=="" || passwordC.trim()=="" || !PasswordValidator.validatePassword(password)){
            model.addAttribute("token",token);
            return "password_change";
        }




        if(!password.equals(passwordC)){
            model.addAttribute("token",token);
            model.addAttribute("errorPasswordC","match");
            return "password_change";
        }

        if(!userRepository.existsByEmail(passwordChangeToken.getEmail())){
            model.addAttribute("errorToken","emailNotFound");
            return "password_change_message";
        }

        User user = userRepository.getUserByEmail(passwordChangeToken.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        passwordChangeTokenRepository.deleteAllByEmail(passwordChangeToken.getEmail());
        return "password_change_message";
    }

}
