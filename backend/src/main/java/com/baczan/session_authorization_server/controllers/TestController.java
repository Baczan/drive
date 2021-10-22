package com.baczan.session_authorization_server.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/test")
public class TestController {


    @GetMapping("/test1")
    public ResponseEntity<?> test1(){
        return new ResponseEntity<>("user", HttpStatus.OK);
    }

    @GetMapping("/test2")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> test2(){
        return new ResponseEntity<>("admin", HttpStatus.OK);
    }

    @PostMapping("/test3")
    public ResponseEntity<?> test3(){
        return new ResponseEntity<>("user", HttpStatus.OK);
    }

}
