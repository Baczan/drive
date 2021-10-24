package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;
import com.baczan.session_authorization_server.repositories.FileRepository;
import com.baczan.session_authorization_server.repositories.FolderRepository;
import com.baczan.session_authorization_server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/folder")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @RequestParam(required = false) UUID parentId, Authentication authentication) {


        if (folderRepository.existsByFolderNameAndParentIdAndUser(folderName, parentId,authentication.getName())) {
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }

        if (parentId == null) {

            Folder folder = new Folder(UUID.randomUUID(), authentication.getName(), folderName, null, null);
            folder = folderRepository.save(folder);
            return new ResponseEntity<>(folder, HttpStatus.OK);

        } else {

            Optional<Folder> optionalFolder = folderRepository.findById(parentId);

            if (optionalFolder.isEmpty()) {
                return new ResponseEntity<>("parent_folder_not_found", HttpStatus.BAD_REQUEST);
            }

            Folder parentFolder = optionalFolder.get();

            Folder folder = new Folder(UUID.randomUUID(), authentication.getName(), folderName, null, parentId);

            if (parentFolder.getAncestry() == null) {
                folder.setAncestry(parentId.toString());
            } else {
                String ancestry = parentFolder.getAncestry() + "/" + parentId;
                folder.setAncestry(ancestry);
            }

            folder = folderRepository.save(folder);
            return new ResponseEntity<>(folder, HttpStatus.OK);

        }
    }

    @DeleteMapping("/deleteFolder")
    public ResponseEntity<?> deleteFolder(@RequestParam UUID folderId, Authentication authentication) {

        Optional<Folder> optionalFolder = folderRepository.findById(folderId);

        if (optionalFolder.isEmpty()) {
            return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
        }

        Folder folder = optionalFolder.get();

        if (!folder.getUser().equals(authentication.getName())) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        String subfolderAncestry;

        if (folder.getAncestry() == null) {
            subfolderAncestry = folder.getId().toString();
        } else {
            subfolderAncestry = folder.getAncestry() + "/" + folder.getId();
        }

        List<Folder> folders = new ArrayList<>();

        folders.add(folder);

        folders.addAll(folderRepository.getAllByAncestryIsStartingWith(subfolderAncestry));

        for (Folder folder1 : folders) {
            try {
                deleteAllFilesFromFolder(folder1);
            } catch (IOException e) {
                return new ResponseEntity<>("io_exception", HttpStatus.BAD_REQUEST);
            }
            folderRepository.delete(folder1);
        }

        return new ResponseEntity<>("deleted", HttpStatus.OK);
    }

    public void deleteAllFilesFromFolder(Folder folder) throws IOException {

        List<FileEntity> files = fileRepository.getAllByFolderId(folder.getId());

        for (FileEntity fileEntity : files) {
            fileService.deleteFile(fileEntity);
        }

    }

    @PostMapping("/changeName")
    public ResponseEntity<?> changeFolderName(@RequestParam UUID folderId, @RequestParam String newName, Authentication authentication) {

        Optional<Folder> optionalFolder = folderRepository.findById(folderId);

        if (optionalFolder.isEmpty()) {
            return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
        }

        Folder folder = optionalFolder.get();

        if (!folder.getUser().equals(authentication.getName())) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        if (folderRepository.existsByFolderNameAndParentIdAndUser(newName, folder.getParentId(),authentication.getName())) {
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }

        folder.setFolderName(newName);

        folder = folderRepository.save(folder);

        return new ResponseEntity<>(folder, HttpStatus.OK);

    }

    @PostMapping("/setFavorite")
    public ResponseEntity<?> setFavorite(@RequestParam List<UUID> folderIds, @RequestParam boolean value, Authentication authentication) {

        List<Folder> folders = new ArrayList<>();

        for (UUID folderId : folderIds) {

            Optional<Folder> optionalFolder = folderRepository.findById(folderId);

            if(optionalFolder.isEmpty()){
                return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
            }

            Folder folder = optionalFolder.get();

            if (!folder.getUser().equals(authentication.getName())) {
                return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            }

            folders.add(folder);
        }

        folders.forEach(folder -> {
            folder.setFavorite(value);
        });




        folders = folderRepository.saveAll(folders);

        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    @GetMapping("/getFavorites")
    public ResponseEntity<?> getFavorites(Authentication authentication){
        return new ResponseEntity<>(folderRepository.getAllByUserAndFavorite(authentication.getName(),true),HttpStatus.OK);
    }

}
