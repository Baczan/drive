package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.dtos.FolderTransferRequestBody;
import com.baczan.session_authorization_server.dtos.TransferFolder;
import com.baczan.session_authorization_server.dtos.TransferFolderResponse;
import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;
import com.baczan.session_authorization_server.repositories.FileRepository;
import com.baczan.session_authorization_server.repositories.FolderRepository;
import com.baczan.session_authorization_server.service.FileService;
import com.baczan.session_authorization_server.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/folder")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @RequestParam(required = false) UUID parentId, Authentication authentication) {


        if (folderRepository.existsByFolderNameAndParentIdAndUser(folderName, parentId, authentication.getName())) {
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

        if (folderRepository.existsByFolderNameAndParentIdAndUser(newName, folder.getParentId(), authentication.getName())) {
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

            if (optionalFolder.isEmpty()) {
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
    public ResponseEntity<?> getFavorites(Authentication authentication) {
        return new ResponseEntity<>(folderRepository.getAllByUserAndFavorite(authentication.getName(), true), HttpStatus.OK);
    }


    @PostMapping("/transferOptions")
    public ResponseEntity<?> getTransferOptions(@RequestParam(required = false) UUID folderId, @RequestBody List<UUID> foldersToTransfer, Authentication authentication) {


        Folder folder = null;

        if (folderId != null) {

            Optional<Folder> optionalFolder = folderRepository.findById(folderId);

            if (optionalFolder.isEmpty()) {
                return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
            }
            folder = optionalFolder.get();

            if (!Objects.equals(folder.getUser(), authentication.getName())) {
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }

        }


        List<Folder> possibleFolders = folderRepository.getAllByParentIdAndUser(folderId, authentication.getName());

        List<TransferFolder> transferFolders = possibleFolders.stream().map(TransferFolder::new).collect(Collectors.toList());


        for (UUID folderToTransferId : foldersToTransfer) {

            Optional<Folder> optionalFolderToTransfer = folderRepository.findById(folderToTransferId);

            if (optionalFolderToTransfer.isEmpty()) {
                return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
            }
            Folder folderToTransfer = optionalFolderToTransfer.get();
            if (!Objects.equals(folderToTransfer.getUser(), authentication.getName())) {
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }


            for (TransferFolder transferFolder : transferFolders) {

                if (transferFolder.isCanBeTransferred()) {

                    transferFolder.setCanBeTransferred(folderService.canFolderBeTransferred(folderToTransfer, transferFolder.getFolder()));

                }

            }

        }

        List<FileEntity> files = fileRepository.getAllByFolderIdAndUser(folderId, authentication.getName());

        TransferFolderResponse transferFolderResponse = new TransferFolderResponse(folder, transferFolders, files);


        return new ResponseEntity<>(transferFolderResponse, HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestParam(required = false) UUID folderId, @RequestBody FolderTransferRequestBody body, Authentication authentication) {


        Folder folder = null;

        if (folderId != null) {

            Optional<Folder> optionalFolder = folderRepository.findById(folderId);

            if (optionalFolder.isEmpty()) {
                return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
            }
            folder = optionalFolder.get();
            if (!Objects.equals(folder.getUser(), authentication.getName())) {
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }


        }


        List<Folder> foldersToTransfer = new ArrayList<>();

        for (UUID folderToTransferId : body.getFolders()) {

            Optional<Folder> optionalFolderToTransfer = folderRepository.findById(folderToTransferId);

            if (optionalFolderToTransfer.isEmpty()) {
                return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
            }

            Folder folderToTransfer = optionalFolderToTransfer.get();

            if (!Objects.equals(folderToTransfer.getUser(), authentication.getName())) {
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }

            foldersToTransfer.add(folderToTransfer);
        }

        List<FileEntity> filesToTransfer = new ArrayList<>();

        for (UUID fileId : body.getFiles()) {

            Optional<FileEntity> optionalFile = fileRepository.findById(fileId);

            if (optionalFile.isEmpty()) {
                return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
            }

            FileEntity fileEntity = optionalFile.get();

            if (!Objects.equals(fileEntity.getUser(), authentication.getName())) {
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }

            filesToTransfer.add(fileEntity);

        }

        for (Folder folderToTransfer : foldersToTransfer) {
            if(!folderService.canFolderBeTransferred(folderToTransfer,folder)){
                return new ResponseEntity<>("cant_be_transfered", HttpStatus.UNAUTHORIZED);
            }
        }


        for (Folder folderToTransfer : foldersToTransfer) {
            folderService.transferFolder(folderToTransfer, folder);
        }

        for (FileEntity fileToTransfer : filesToTransfer) {
            fileToTransfer.setFolderId(folderId);
        }

        fileRepository.saveAll(filesToTransfer);

        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}
