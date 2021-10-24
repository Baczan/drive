package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    boolean existsByFilenameAndFolderIdAndUser(String filename,UUID folderId,String email);

    List<FileEntity> getAllByFolderIdAndUser(UUID folderId,String user);

    List<FileEntity> getAllByFolderId(UUID folderId);
}
