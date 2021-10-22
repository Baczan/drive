package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.entities.Folder;
import com.baczan.session_authorization_server.repositories.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/folder")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @RequestParam(required = false) UUID parentId, Authentication authentication){


        if(folderRepository.existsByFolderNameAndParentId(folderName,parentId)){
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }

        if(parentId==null){

            Folder folder = new Folder(UUID.randomUUID(),authentication.getName(),folderName,null,null);
            folder = folderRepository.save(folder);
            return new ResponseEntity<>(folder,HttpStatus.OK);

        }else {

            Optional<Folder> optionalFolder = folderRepository.findById(parentId);

            if(optionalFolder.isEmpty()){
                return new ResponseEntity<>("parent_folder_not_found",HttpStatus.BAD_REQUEST);
            }

            Folder parentFolder = optionalFolder.get();

            Folder folder = new Folder(UUID.randomUUID(),authentication.getName(),folderName,null,parentId);

            if(parentFolder.getAncestry()==null){
                folder.setAncestry(parentId.toString());
            }else {
                String ancestry = parentFolder.getAncestry()+"/"+parentId;
                folder.setAncestry(ancestry);
            }

            folder = folderRepository.save(folder);
            return new ResponseEntity<>(folder,HttpStatus.OK);

        }
    }


}
