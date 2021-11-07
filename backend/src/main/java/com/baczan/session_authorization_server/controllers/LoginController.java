package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.dtos.TestFile;
import com.baczan.session_authorization_server.entities.Authority;
import com.baczan.session_authorization_server.entities.Folder;
import com.baczan.session_authorization_server.entities.User;
import com.baczan.session_authorization_server.exceptions.FolderNotFoundException;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.exceptions.UnauthorizedException;
import com.baczan.session_authorization_server.helpers.ResponseHelper;
import com.baczan.session_authorization_server.repositories.FolderRepository;
import com.baczan.session_authorization_server.repositories.UserRepository;
import com.baczan.session_authorization_server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class LoginController implements ErrorController {


    private final Environment environment;

    private final UserRepository userRepository;

    private final FolderController folderController;

    private final FileService fileService;

    public LoginController(Environment environment, UserRepository userRepository, FolderController folderController, FileService fileService) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.folderController = folderController;
        this.fileService = fileService;
    }

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


    //Create random user for testing purpose
    @GetMapping("/testLogin")
    public String testLogin(HttpServletRequest request,
                            HttpServletResponse response,
                            CsrfToken csrfToken) throws TierNotFoundException, IOException, FolderNotFoundException, UnauthorizedException {



        //Create user with random email
        User user = new User();

        Random random = new Random();

        while (user.getEmail()==null){

            String email = "testowy"+random.nextInt(9999)+"@test.com";

            if(!userRepository.existsByEmailAndEmailActivatedIsTrue(email)){
                user.setEmail(email);
            }

        }

        user.setEmailActivated(true);
        user.setAuthorities(Collections.singletonList(new Authority("ROLE_USER", user)));

        user = userRepository.save(user);

        List<GrantedAuthority> authorities = user.getAuthorities().stream().map(authority -> new SimpleGrantedAuthority(authority.getRole())).collect(Collectors.toList());


        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")
                .authorities(authorities)
                .build();



        //Create authentication and put it in security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails,null,authorities);


        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);


        //Set cookies to be send with a response
        ResponseHelper.setCookies(response,authentication,csrfToken);

        //Give user some files and folders
        populateTestUserDrive(authentication);

        //Redirect user back to application
        String redirectUrl;

        if(request.getSession().getAttribute("redirectUrl") == null){
            redirectUrl = environment.getProperty("app.redirectUrl");
        }else{
            redirectUrl = request.getSession().getAttribute("redirectUrl").toString();
        }

        return "redirect:"+redirectUrl;
    }


    private void populateTestUserDrive(Authentication authentication) throws IOException, TierNotFoundException, FolderNotFoundException, UnauthorizedException {

        Folder backupFolder = (Folder) folderController.createFolder("Backup",null,authentication).getBody();
        Folder photoFolder  = (Folder) folderController.createFolder("Zdjęcia",null,authentication).getBody();
        Folder dogFolder = (Folder) folderController.createFolder("Pieski",photoFolder.getId(),authentication).getBody();
        Folder vacationFolder = (Folder) folderController.createFolder("Wakacje 2021",photoFolder.getId(),authentication).getBody();

        folderController.setFavorite(Collections.singletonList(backupFolder.getId()),true,authentication);
        folderController.setFavorite(Collections.singletonList(vacationFolder.getId()),true,authentication);

        List<TestFile> testFiles = List.of(
                new TestFile("cottages.jpeg", vacationFolder.getId()),
                new TestFile("resort.jpeg", vacationFolder.getId()),
                new TestFile("seashore.jpeg", vacationFolder.getId()),
                new TestFile("palm-trees.jpeg", vacationFolder.getId()),
                new TestFile("golden1.jpeg", dogFolder.getId()),
                new TestFile("golden2.jpeg", dogFolder.getId()),
                new TestFile("golden3.jpeg", dogFolder.getId()),
                new TestFile("golden4.jpeg", dogFolder.getId()),
                new TestFile("golden5.jpeg", dogFolder.getId()),
                new TestFile("eifeel-tower.jpeg", null),
                new TestFile("norway.jpeg", null),
                new TestFile("Wypracowanie.docx", null),
                new TestFile("notatki.txt", null),
                new TestFile("2021-02-05.xls", backupFolder.getId()),
                new TestFile("2021-02-06.xls", backupFolder.getId()),
                new TestFile("2021-02-07.xls", backupFolder.getId()),
                new TestFile("2021-02-08.xls", backupFolder.getId()),
                new TestFile("2021-02-09.xls", backupFolder.getId()),
                new TestFile("2021-02-10.xls", backupFolder.getId()),
                new TestFile("2021-02-11.xls", backupFolder.getId())
        );

        testFiles.parallelStream().forEach(testFile -> {

            try {
                MultipartFile multipartFile = new MockMultipartFile(testFile.name,
                        testFile.name,
                        Files.probeContentType(fileService.getPathToTestFile(testFile.name)),
                        Files.newInputStream(fileService.getPathToTestFile(testFile.name)));

                fileService.saveFile(multipartFile,testFile.parentId,authentication,true);

            } catch (IOException ignored) {
            }
        });






    }



}
