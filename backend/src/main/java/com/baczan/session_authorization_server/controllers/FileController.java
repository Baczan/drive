package com.baczan.session_authorization_server.controllers;

import com.baczan.session_authorization_server.dtos.FilesAndFoldersDTO;
import com.baczan.session_authorization_server.dtos.StorageSpaceDTO;
import com.baczan.session_authorization_server.dtos.ZipFile;
import com.baczan.session_authorization_server.dtos.ZipFolder;
import com.baczan.session_authorization_server.entities.*;
import com.baczan.session_authorization_server.exceptions.FileNotFoundException;
import com.baczan.session_authorization_server.exceptions.FolderNotFoundException;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.exceptions.UnauthorizedException;
import com.baczan.session_authorization_server.repositories.*;
import com.baczan.session_authorization_server.service.FileService;
import com.baczan.session_authorization_server.service.FolderService;
import com.baczan.session_authorization_server.service.StripeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/file")
public class FileController {

    private final FileService fileService;

    private final FileRepository fileRepository;

    private final FolderRepository folderRepository;

    private final ZipRepository zipRepository;

    private final StripeService stripeService;

    private final UserRepository userRepository;

    private final FolderService folderService;

    public FileController(FileService fileService, FileRepository fileRepository, FolderRepository folderRepository, ZipRepository zipRepository, StripeService stripeService, UserRepository userRepository, FolderService folderService) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.zipRepository = zipRepository;
        this.stripeService = stripeService;
        this.userRepository = userRepository;
        this.folderService = folderService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file, @RequestParam(required = false) UUID folderId, Authentication authentication) throws TierNotFoundException {


        //Check if there is no conflicting file names
        if (fileRepository.existsByFilenameAndFolderIdAndUser(file.getOriginalFilename(), folderId,authentication.getName())) {
            return new ResponseEntity<>("not_unique", HttpStatus.BAD_REQUEST);
        }


        //Check is user have enough space
        User user = userRepository.getUserByEmail(authentication.getName());

        StorageSpaceDTO storageSpaceDTO = stripeService.getStorageSpace(user.getEmail());

        if((storageSpaceDTO.getUsedSpace()+file.getSize())>storageSpaceDTO.getAvailableSpace()){
            return new ResponseEntity<>("not_enough_space", HttpStatus.BAD_REQUEST);
        }


        try {
            return new ResponseEntity<>(fileService.saveFile(file, folderId, authentication,false), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("io_error", HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(@RequestParam(required = false) UUID folderId, Authentication authentication) {

        List<Folder> folders = folderRepository.getAllByParentIdAndUser(folderId, authentication.getName());
        List<FileEntity> files = fileRepository.getAllByFolderIdAndUser(folderId, authentication.getName());

        FilesAndFoldersDTO filesAndFoldersDTO = new FilesAndFoldersDTO(folders, files);

        //Get info about parent folder
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
    public ResponseEntity<?> getThumbnail(@RequestParam UUID fileId, Authentication authentication) throws FileNotFoundException, UnauthorizedException {

        FileEntity fileEntity = fileService.getFileEntity(fileId, authentication);

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
    public ResponseEntity<?> downloadMultiple(@RequestParam List<UUID> filesId, @RequestParam List<UUID> foldersId, @RequestParam(required = false) UUID parentId, Authentication authentication) throws FolderNotFoundException, UnauthorizedException, FileNotFoundException {

        List<ZipFile> zipFiles = new ArrayList<>();

        Map<String, String> folderNames = new HashMap<String, String>();

        List<Folder> folders = new ArrayList<>();

        for (UUID id : foldersId) {
            folders.add(folderService.getFolder(id,authentication));
        }

        int deleteFromAncestry = 0;

        if (parentId != null) {

            Folder parentFolder = folderService.getFolder(parentId,authentication);

            //Save parent folder
            folderNames.put(parentFolder.getId().toString(), parentFolder.getFolderName());


            //Calculate how many ancestors you have to delete to get relative path
            deleteFromAncestry += 1;

            if (parentFolder.getAncestry() != null) {

                deleteFromAncestry += parentFolder.getAncestry().split("/").length;
            }

        }



        List<ZipFolder> zipFolders = new ArrayList<>();

        for (Folder folder : folders) {

            //Get all files that are directly in the folder
            List<FileEntity> fileEntities = fileRepository.getAllByFolderId(folder.getId());

            for (FileEntity fileEntity:fileEntities) {
                zipFiles.add(new ZipFile(fileEntity,folder.getFolderName()));
            }


            //Save folder name
            folderNames.put(folder.getId().toString(), folder.getFolderName());

            String subfolderAncestry;

            if (folder.getAncestry() == null) {
                subfolderAncestry = folder.getId().toString();
            } else {
                subfolderAncestry = folder.getAncestry() + "/" + folder.getId();
            }


            //Get all the sub folders and adjust their ancestry to be relative
            List<Folder> subFolders = folderRepository.getAllByAncestryIsStartingWith(subfolderAncestry);

            for (Folder subFolder : subFolders) {
                List<String> ancestryList = Arrays.asList(subFolder.getAncestry().split("/"));
                ancestryList = ancestryList.subList(deleteFromAncestry, ancestryList.size());

                folderNames.put(subFolder.getId().toString(), subFolder.getFolderName());
                zipFolders.add(new ZipFolder(subFolder.getFolderName(), subFolder.getId(), ancestryList));

            }

        }



        for (ZipFolder zipFolder : zipFolders) {

            //Map ids of the folders to their names
            zipFolder.setAncestryList(zipFolder.getAncestryList().stream().map(folderNames::get).collect(Collectors.toList()));

            //Build ancestry based on folder names
            StringBuilder ancestry= new StringBuilder();

            for(int i=0;i<zipFolder.getAncestryList().size();i++){
                if(i!=0){
                    ancestry.append("/");
                }

                ancestry.append(zipFolder.getAncestryList().get(i));
            }

            ancestry.append("/").append(zipFolder.getFolderName());


            //Add all files of sub folder with their relative ancestry
            List<FileEntity> fileEntities = fileRepository.getAllByFolderId(zipFolder.getId());

            for (FileEntity fileEntity:fileEntities) {
                zipFiles.add(new ZipFile(fileEntity, ancestry.toString()));
            }

        }

        //Add files that are not in any folder
        for (UUID id: filesId){
            zipFiles.add(new ZipFile(fileService.getFileEntity(id,authentication), null));
        }


        //Check authentication of all files
        for (ZipFile zipFile: zipFiles){

            if(!authentication.getName().equals(zipFile.getFileEntity().getUser())){
                return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
            }

        }


        //Calculate the final size of all files
        //If the total sum is 0 change it to 1 to avoid dividing by 0
        long filesSize = zipFiles.stream().mapToLong(zipFile -> zipFile.getFileEntity().getSize()).sum();

        if(filesSize==0){
            filesSize = 1;
        }

        //Save info about zip and start process in the background
        ZipInfo zipInfo = new ZipInfo(UUID.randomUUID(),authentication.getName(),filesSize);
        zipRepository.save(zipInfo);

        fileService.generateZipAsync(zipInfo,zipFiles,authentication.getName());

        return new ResponseEntity<>(zipInfo, HttpStatus.OK);
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam UUID fileId,@RequestParam(required = false) boolean displayPhoto, Authentication authentication) throws FileNotFoundException, UnauthorizedException {

        FileEntity fileEntity = fileService.getFileEntity(fileId,authentication);

        //Create headers object
        HttpHeaders headers = new HttpHeaders();

        String contentDispositionMode = "attachment;";

        if(displayPhoto){
            contentDispositionMode = "inline;";
        }

        //Set filename
        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDispositionMode+" filename=" + fileEntity.getFilename());

        //Set file type
        headers.add(HttpHeaders.CONTENT_TYPE, URLConnection.guessContentTypeFromName(fileEntity.getFilename()));

        headers.add(HttpHeaders.CACHE_CONTROL,"max-age=86400, no-transform");

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
    public ResponseEntity<?> deleteFile(@RequestParam UUID fileId, Authentication authentication) throws FileNotFoundException, UnauthorizedException {

        FileEntity fileEntity = fileService.getFileEntity(fileId,authentication);

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
