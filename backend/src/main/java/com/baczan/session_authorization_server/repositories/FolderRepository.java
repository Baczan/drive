package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    boolean existsByFolderNameAndParentId(String folderName,UUID parentID);

    List<Folder> getAllByParentIdAndUser(UUID parentId,String user);
    

    List<Folder> getAllByAncestryIsStartingWith(String ancestry);

}
