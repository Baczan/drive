package com.baczan.session_authorization_server.service;

import com.baczan.session_authorization_server.entities.Folder;
import com.baczan.session_authorization_server.repositories.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    public boolean canFolderBeTransferred(Folder folderToTransfer,Folder destinationFolder){

        return !getFolderFullPath(destinationFolder).startsWith(getFolderFullPath(folderToTransfer));
    }


    private String getFolderFullPath(Folder folder){


        if(folder.getAncestry()==null){
            return folder.getId().toString();
        }

        return folder.getAncestry()+"/"+folder.getId().toString();
    }

    public void transferFolder(Folder folderToTransfer,Folder destinationFolder){

        
        List<Folder> children = folderRepository.getAllByAncestryIsStartingWith(getFolderFullPath(folderToTransfer));

        if(destinationFolder==null){
            folderToTransfer.setAncestry(null);
            folderToTransfer.setParentId(null);

        }else{
            folderToTransfer.setAncestry(getFolderFullPath(destinationFolder));
            folderToTransfer.setParentId(destinationFolder.getId());
        }

        for (Folder child:children) {
            StringBuilder newAncestry = new StringBuilder(getFolderFullPath(folderToTransfer));
            List<String> ancestryList = List.of(child.getAncestry().split("/"));
            int indexOfParent = ancestryList.indexOf(folderToTransfer.getId().toString());
            if(indexOfParent!=(ancestryList.size()-1)){

                ancestryList = ancestryList.subList(indexOfParent,ancestryList.size());

                for (String ancestryElement:ancestryList) {
                    newAncestry.append("/").append(ancestryElement);
                }
            }
            child.setAncestry(newAncestry.toString());
        }

        folderRepository.saveAll(children);
        folderRepository.save(folderToTransfer);
    }

}
