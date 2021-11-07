package com.baczan.session_authorization_server.service;

import com.baczan.session_authorization_server.dtos.StorageSpaceDTO;
import com.baczan.session_authorization_server.dtos.ZipFile;
import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.SubscriptionEntity;
import com.baczan.session_authorization_server.entities.User;
import com.baczan.session_authorization_server.entities.ZipInfo;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.repositories.FileRepository;
import com.baczan.session_authorization_server.repositories.UserRepository;
import com.baczan.session_authorization_server.repositories.ZipRepository;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    @Autowired
    private Environment environment;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ZipRepository zipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripeService stripeService;

    private final ReentrantLock lock = new ReentrantLock();

    private final List<String> acceptableImageFormats = Arrays.asList(
            "png",
            "jpg",
            "jpeg"
    );


    @Transactional
    public FileEntity saveFile(MultipartFile file, UUID folderId, Authentication authentication,boolean testFile) throws IOException {

        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        boolean hasThumbnail = false;

        if(fileExtension!=null){
            hasThumbnail= acceptableImageFormats.contains(fileExtension.toLowerCase());
        }



        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(UUID.randomUUID());
        fileEntity.setFilename(file.getOriginalFilename());
        fileEntity.setUser(authentication.getName());
        fileEntity.setSize(file.getSize());
        fileEntity.setDate(new Date());
        fileEntity.setHasThumbnail(hasThumbnail);


        fileEntity.setFolderId(folderId);


        file.transferTo(getPath(fileEntity));


        if (hasThumbnail) {

            if(testFile){
                createTestThumbnail(fileEntity);
            }else{
                createThumbnail(fileEntity);
            }


        }

        fileEntity = fileRepository.save(fileEntity);



        //saveStorageSpace(authentication.getName(),fileEntity.getSize());

        this.userRepository.addStorageSpace(fileEntity.getUser(),fileEntity.getSize());

        try {
            simpMessagingTemplate.convertAndSendToUser(authentication.getName(), "/queue/storageSpace", stripeService.getStorageSpace(fileEntity.getUser()));
        } catch (TierNotFoundException ignored) {
        }

        return fileEntity;
    }

    @Transactional
    public void deleteFile(FileEntity fileEntity) throws IOException {

        Files.deleteIfExists(getPath(fileEntity));

        if(fileEntity.isHasThumbnail()){
            Files.deleteIfExists(getPathThumbnail(fileEntity));
        }

        fileRepository.delete(fileEntity);

        //saveStorageSpace(fileEntity.getUser(),-fileEntity.getSize());

        this.userRepository.addStorageSpace(fileEntity.getUser(),-fileEntity.getSize());

        try {
            simpMessagingTemplate.convertAndSendToUser(fileEntity.getUser(), "/queue/storageSpace", stripeService.getStorageSpace(fileEntity.getUser()));
        } catch (TierNotFoundException ignored) {
        }
    }

    private void createThumbnail(FileEntity fileEntity) throws IOException {

        BufferedImage srcImage = ImageIO.read(getPath(fileEntity).toFile());
        BufferedImage scaledImage = Scalr.resize(srcImage, 200, 200);
        ImageIO.write(scaledImage, "png", getPathThumbnail(fileEntity).toFile());
    }

    public Path getPath(FileEntity fileEntity) {

        return Paths.get(environment.getProperty("app.file.location") + "original/" + fileEntity.getId());
    }

    public Path getPathThumbnail(FileEntity fileEntity) {

        return Paths.get(environment.getProperty("app.file.location") + "thumbnails/" + fileEntity.getId());
    }

    public Path getPathZip(ZipInfo zipInfo){
        return Paths.get(environment.getProperty("app.file.location") + "zip/" + zipInfo.getId()+".zip");
    }


    @Async
    public void generateZipAsync(ZipInfo zipInfo, List<ZipFile> zipFiles,String user){


        try {

            //Generate output zip
            ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(getPathZip(zipInfo)));

            long zipSizeCompleted = 0L;

            for (ZipFile zipFile:zipFiles){

                String fullPath = "";

                if (zipFile.getAncestry()==null){
                    fullPath = zipFile.getFileEntity().getFilename();
                }else {
                    fullPath = zipFile.getAncestry()+"/"+zipFile.getFileEntity().getFilename();
                }

                //Create new zip entry
                ZipEntry zipEntry = new ZipEntry(fullPath);

                //Put next entry to zip
                zipOutputStream.putNextEntry(zipEntry);

                //Copy file to zip
                Files.copy(getPath(zipFile.getFileEntity()),zipOutputStream);

                //Close entry
                zipOutputStream.closeEntry();

                //Calculate completed zip size
                zipSizeCompleted+=zipFile.getFileEntity().getSize();
                

                zipInfo.setProgress((float)zipSizeCompleted/zipInfo.getSize());

                simpMessagingTemplate.convertAndSendToUser(user, "/queue/zip", zipInfo);

            }

            //Close zip
            zipOutputStream.close();

            zipInfo.setCompleted(true);
            zipRepository.save(zipInfo);
            simpMessagingTemplate.convertAndSendToUser(user, "/queue/zip", zipInfo);

        } catch (IOException e) {

            zipInfo.setError(true);
            zipRepository.save(zipInfo);
            simpMessagingTemplate.convertAndSendToUser(user, "/queue/zip", zipInfo);
        }


    }


    private void createTestThumbnail(FileEntity fileEntity) throws IOException {

        File thumbnailFile = getPathThumbnail(fileEntity).toFile();
        File testThumbnailFile = getPathToTestThumbnails(fileEntity.getFilename()).toFile();
        FileUtils.copyFile(testThumbnailFile,thumbnailFile);

    }

    public Path getPathToTestFile(String name){
        return Paths.get(environment.getProperty("app.file.location") + "test/" + name);
    }

    public Path getPathToTestThumbnails(String name){
        return Paths.get(environment.getProperty("app.file.location") + "testThumbnails/" + name);
    }



}
