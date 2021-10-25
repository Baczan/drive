import { Injectable } from '@angular/core';
import {FileService} from "./file.service";

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  displayMenu:boolean = false;
  left:number = 0
  top:number = 0

  moveToTop:boolean = false;

  constructor(private fileService:FileService) { }

  starIconMode(){
    let value = false;

    this.fileService.selectedFolders.forEach(folder=>{
      if(!folder.favorite){
        value = true;
      }
    })

    return value;
  }

  calculateHeight(){
    let height = 110;

    if(this.fileService.selectedFolders.length==1 && this.fileService.selectedFiles.length==0){
      height+=44;
    }


    if(this.fileService.selectedFolders.length>0 && this.fileService.selectedFiles.length==0 && this.starIconMode()){
      height+=44;
    }

    if(this.fileService.selectedFolders.length>0 && this.fileService.selectedFiles.length==0 && !this.starIconMode()){
      height+=44;
    }

    return height;

  }
}
