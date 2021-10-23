package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.dtos.FilesAndFoldersDTO;
import com.baczan.session_authorization_server.dtos.StorageSpaceDTO;
import com.baczan.session_authorization_server.dtos.ZipFile;
import com.baczan.session_authorization_server.dtos.ZipFolder;
import com.baczan.session_authorization_server.entities.*;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.repositories.*;
import com.baczan.session_authorization_server.service.FileService;
import com.baczan.session_authorization_server.service.StripeService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ZipRepository zipRepository;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file, @RequestParam(required = false) UUID folderId, Authentication authentication) throws TierNotFoundException {


        if (fileRepository.existsByFilenameAndFolderId(file.getOriginalFilename(), folderId)) {
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.getUserByEmail(authentication.getName());

        StorageSpaceDTO storageSpaceDTO = stripeService.getStorageSpace(user.getEmail());

        if((storageSpaceDTO.getUsedSpace()+file.getSize())>storageSpaceDTO.getAvailableSpace()){
            return new ResponseEntity<>("not_enough_space", HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(fileService.saveFile(file, folderId, authentication), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("io_error", HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(@RequestParam(required = false) UUID folderId, Authentication authentication) {

        List<Folder> folders = folderRepository.getAllByParentIdAndUser(folderId, authentication.getName());
        List<FileEntity> files = fileRepository.getAllByFolderIdAndUser(folderId, authentication.getName());

        FilesAndFoldersDTO filesAndFoldersDTO = new FilesAndFoldersDTO(folders, files);

        if (folderId != null) {

            Optional<Folder> optionalFolder = folderRepository.findById(folderId);

            if (optionalFolder.isEmpty()) {
                return new ResponseEntity<>("wrong_id", HttpStatus.BAD_REQUEST);
            }

            filesAndFoldersDTO.setParentFolder(optionalFolder.get());

        }

        return new ResponseEntity<>(filesAndFoldersDTO, HttpStatus.OK);

    }

    @GetMapping("/getThumbnail")
    public ResponseEntity<?> getThumbnail(@RequestParam UUID fileId, Authentication authentication) {

        Optional<FileEntity> optionalFile = fileRepository.findById(fileId);

        if (optionalFile.isEmpty()) {
            return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
        }

        FileEntity fileEntity = optionalFile.get();

        if (!fileEntity.getUser().equals(authentication.getName())) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        if (!fileEntity.isHasThumbnail()) {
            return new ResponseEntity<>("do_not_have_thumbnail", HttpStatus.BAD_REQUEST);
        }


        //Create headers object
        HttpHeaders headers = new HttpHeaders();

        //Set filename
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileEntity.getId() + ".png");

        //Set file type
        headers.add(HttpHeaders.CONTENT_TYPE, "image/png");

        headers.add(HttpHeaders.CACHE_CONTROL, "public, max-age=604800, immutable");

        try {
            //Read file
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(fileService.getPathThumbnail(fileEntity)));

            //Response
            return ResponseEntity
                    .ok() //Response status OK
                    .headers(headers) //Headers
                    .contentLength(resource.contentLength()) //File size
                    .body(resource); //File

        } catch (IOException e) {
            return new ResponseEntity<>("io_exception", HttpStatus.BAD_REQUEST);
        }


    }

    @GetMapping("/downloadMultiple")
    public ResponseEntity<?> downloadMultiple(@RequestParam List<UUID> filesId, @RequestParam List<UUID> foldersId, @RequestParam(required = false) UUID parentId, Authentication authentication) {

        List<ZipFile> zipFiles = new ArrayList<>();

        Map<String, String> map = new HashMap<String, String>();

        List<Folder> folders = new ArrayList<>();

        for (UUID id : foldersId) {

            Optional<Folder> optionalFolder = folderRepository.findById(id);

            if (optionalFolder.isEmpty()) {
                return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
            }

            folders.add(optionalFolder.get());
        }

        int deleteFromAncestry = 0;

        if (parentId != null) {
            Optional<Folder> parentFolderOptional = folderRepository.findById(parentId);
            if (parentFolderOptional.isEmpty()) {
                return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
            }

            Folder parentFolder = parentFolderOptional.get();

            deleteFromAncestry += 1;
            map.put(parentFolder.getId().toString(), parentFolder.getFolderName());

            if (parentFolder.getAncestry() != null) {

                deleteFromAncestry += parentFolder.getAncestry().split("/").length;

            }

        }

        List<ZipFolder> zipFolders = new ArrayList<>();

        for (Folder folder : folders) {

            List<FileEntity> fileEntities = fileRepository.getAllByFolderId(folder.getId());

            for (FileEntity fileEntity:fileEntities) {

                zipFiles.add(new ZipFile(fileEntity,folder.getFolderName()));

            }


            map.put(folder.getId().toString(), folder.getFolderName());

            String subfolderAncestry;

            if (folder.getAncestry() == null) {
                subfolderAncestry = folder.getId().toString();
            } else {
                subfolderAncestry = folder.getAncestry() + "/" + folder.getId();
            }

            List<Folder> subFolders = folderRepository.getAllByAncestryIsStartingWith(subfolderAncestry);

            for (Folder subFolder : subFolders) {
                List<String> ancestryList = Arrays.asList(subFolder.getAncestry().split("/"));
                ancestryList = ancestryList.subList(deleteFromAncestry, ancestryList.size());

                map.put(subFolder.getId().toString(), subFolder.getFolderName());
                zipFolders.add(new ZipFolder(subFolder.getFolderName(), subFolder.getId(), ancestryList));

            }

        }



        for (ZipFolder zipFolder : zipFolders) {

            zipFolder.setAncestryList(zipFolder.getAncestryList().stream().map(map::get).collect(Collectors.toList()));

            String ancestry="";

            for(int i=0;i<zipFolder.getAncestryList().size();i++){
                if(i!=0){
                    ancestry+="/";
                }

                ancestry+=zipFolder.getAncestryList().get(i);
            }

            ancestry+="/"+zipFolder.getFolderName();

            List<FileEntity> fileEntities = fileRepository.getAllByFolderId(zipFolder.getId());

            for (FileEntity fileEntity:fileEntities) {

                zipFiles.add(new ZipFile(fileEntity,ancestry));

            }

        }

        for (UUID id: filesId){

            Optional<FileEntity> optionalFileEntity = fileRepository.findById(id);

            if(optionalFileEntity.isEmpty()){
                return new ResponseEntity<>("error",HttpStatus.BAD_REQUEST);
            }

            zipFiles.add(new ZipFile(optionalFileEntity.get(),null));
        }

        for (ZipFile zipFile: zipFiles){

            if(!authentication.getName().equals(zipFile.getFileEntity().getUser())){
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }

        }



        long filesSize = zipFiles.stream().mapToLong(zipFile -> zipFile.getFileEntity().getSize()).sum();

        ZipInfo zipInfo = new ZipInfo(UUID.randomUUID(),authentication.getName(),filesSize);
        zipRepository.save(zipInfo);

        fileService.generateZipAsync(zipInfo,zipFiles,authentication.getName());

        return new ResponseEntity<>(zipInfo, HttpStatus.OK);
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam UUID fileId, Authentication authentication) {

        Optional<FileEntity> optionalFile = fileRepository.findById(fileId);

        if (optionalFile.isEmpty()) {
            return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
        }

        FileEntity fileEntity = optionalFile.get();

        if (!fileEntity.getUser().equals(authentication.getName())) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        //Create headers object
        HttpHeaders headers = new HttpHeaders();

        //Set filename
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.getFilename());

        //Set file type
        headers.add(HttpHeaders.CONTENT_TYPE, URLConnection.guessContentTypeFromName(fileEntity.getFilename()));

        try {
            //Read file
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(fileService.getPath(fileEntity)));


            //Response
            return ResponseEntity
                    .ok() //Response status OK
                    .headers(headers) //Headers
                    .contentLength(resource.contentLength()) //File size
                    .body(resource); //File

        } catch (IOException e) {
            return new ResponseEntity<>("io_exception", HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/downloadZip")
    public ResponseEntity<?> downloadZip(@RequestParam UUID zipId, Authentication authentication) {

        Optional<ZipInfo> optionalZipInfo = zipRepository.findById(zipId);

        if (optionalZipInfo.isEmpty()) {
            return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
        }

        ZipInfo zipInfo = optionalZipInfo.get();

        if (!zipInfo.getUser().equals(authentication.getName())) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        //Create headers object
        HttpHeaders headers = new HttpHeaders();

        //Set filename
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=files-" + zipInfo.getDate().toString()+".zip");

        //Set file type
        headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

        try {
            //Read file
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(fileService.getPathZip(zipInfo)));

            Files.deleteIfExists(fileService.getPathZip(zipInfo));

            zipRepository.delete(zipInfo);

            //Response
            return ResponseEntity
                    .ok() //Response status OK
                    .headers(headers) //Headers
                    .contentLength(resource.contentLength()) //File size
                    .body(resource); //File

        } catch (IOException e) {
            return new ResponseEntity<>("io_exception", HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@RequestParam UUID fileId, Authentication authentication){

        Optional<FileEntity> optionalFile = fileRepository.findById(fileId);

        if (optionalFile.isEmpty()) {
            return new ResponseEntity<>("not_found", HttpStatus.BAD_REQUEST);
        }

        FileEntity fileEntity = optionalFile.get();

        if (!fileEntity.getUser().equals(authentication.getName())) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        try {
            fileService.deleteFile(fileEntity);

            return new ResponseEntity<>("deleted", HttpStatus.OK);

        } catch (IOException e) {
            return new ResponseEntity<>("io_exception", HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/storageSpace")
    public ResponseEntity<?> getStorageSpace(Authentication authentication) throws TierNotFoundException {

        return new ResponseEntity<>(stripeService.getStorageSpace(authentication.getName()),HttpStatus.OK);
    }

}
