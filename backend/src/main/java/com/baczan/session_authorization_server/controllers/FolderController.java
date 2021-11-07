package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.dtos.FolderTransferRequestBody;
import com.baczan.session_authorization_server.dtos.TransferFolder;
import com.baczan.session_authorization_server.dtos.TransferFolderResponse;
import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;
import com.baczan.session_authorization_server.exceptions.FileNotFoundException;
import com.baczan.session_authorization_server.exceptions.FolderNotFoundException;
import com.baczan.session_authorization_server.exceptions.UnauthorizedException;
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

    private final FolderRepository folderRepository;

    private final FileRepository fileRepository;

    private final FileService fileService;

    private final FolderService folderService;

    public FolderController(FolderRepository folderRepository, FileRepository fileRepository, FileService fileService, FolderService folderService) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
        this.folderService = folderService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @RequestParam(required = false) UUID parentId, Authentication authentication) throws FolderNotFoundException, UnauthorizedException {

        //Check if the name is unique in current folder
        if (folderRepository.existsByFolderNameAndParentIdAndUser(folderName, parentId, authentication.getName())) {
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }

        if (parentId == null) {

            Folder folder = new Folder(UUID.randomUUID(), authentication.getName(), folderName, null, null);
            folder = folderRepository.save(folder);
            return new ResponseEntity<>(folder, HttpStatus.OK);

        } else {

            Folder parentFolder = folderService.getFolder(parentId, authentication);

            Folder folder = new Folder(UUID.randomUUID(), authentication.getName(), folderName, null, parentId);

            //Compose ancestry
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
    public ResponseEntity<?> deleteFolder(@RequestParam UUID folderId, Authentication authentication) throws FolderNotFoundException, UnauthorizedException {

        Folder folder = folderService.getFolder(folderId, authentication);

        String subfolderAncestry;

        if (folder.getAncestry() == null) {
            subfolderAncestry = folder.getId().toString();
        } else {
            subfolderAncestry = folder.getAncestry() + "/" + folder.getId();
        }


        //Delete all folders and their files
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
    public ResponseEntity<?> changeFolderName(@RequestParam UUID folderId, @RequestParam String newName, Authentication authentication) throws FolderNotFoundException, UnauthorizedException {


        Folder folder = folderService.getFolder(folderId, authentication);

        if (folderRepository.existsByFolderNameAndParentIdAndUser(newName, folder.getParentId(), authentication.getName())) {
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }

        folder.setFolderName(newName);

        folder = folderRepository.save(folder);

        return new ResponseEntity<>(folder, HttpStatus.OK);

    }

    @PostMapping("/setFavorite")
    public ResponseEntity<?> setFavorite(@RequestParam List<UUID> folderIds, @RequestParam boolean value, Authentication authentication) throws FolderNotFoundException, UnauthorizedException {

        List<Folder> folders = new ArrayList<>();

        for (UUID folderId : folderIds) {

            Folder folder = folderService.getFolder(folderId, authentication);
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
    public ResponseEntity<?> getTransferOptions(@RequestParam(required = false) UUID folderId, @RequestBody List<UUID> foldersToTransfer, Authentication authentication) throws FolderNotFoundException, UnauthorizedException {


        Folder folder = null;

        if (folderId != null) {
            folder = folderService.getFolder(folderId, authentication);
        }


        List<Folder> possibleFolders = folderRepository.getAllByParentIdAndUser(folderId, authentication.getName());

        List<TransferFolder> transferFolders = possibleFolders.stream().map(TransferFolder::new).collect(Collectors.toList());


        for (UUID folderToTransferId : foldersToTransfer) {

            Folder folderToTransfer = folderService.getFolder(folderToTransferId, authentication);

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
    public ResponseEntity<?> transfer(@RequestParam(required = false) UUID folderId, @RequestBody FolderTransferRequestBody body, Authentication authentication) throws FolderNotFoundException, UnauthorizedException, FileNotFoundException {


        Folder folder = null;

        if (folderId != null) {

            folder = folderService.getFolder(folderId,authentication);
        }


        List<Folder> foldersToTransfer = new ArrayList<>();

        for (UUID folderToTransferId : body.getFolders()) {

            Folder folderToTransfer = folderService.getFolder(folderToTransferId,authentication);

            foldersToTransfer.add(folderToTransfer);
        }

        List<FileEntity> filesToTransfer = new ArrayList<>();

        for (UUID fileId : body.getFiles()) {

            FileEntity fileEntity = fileService.getFileEntity(fileId,authentication);
            filesToTransfer.add(fileEntity);
        }


        //Check if folder can be transferred
        for (Folder folderToTransfer : foldersToTransfer) {

            if (folder != null && !folderService.canFolderBeTransferred(folderToTransfer, folder)) {
                return new ResponseEntity<>("cant_be_transfered", HttpStatus.UNAUTHORIZED);
            }
        }



        //Transfer folders and files
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
