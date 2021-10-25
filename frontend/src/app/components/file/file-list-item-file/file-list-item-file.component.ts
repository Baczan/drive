import {Component, Input, OnInit} from '@angular/core';
import {Folder} from "../../../models/Folder";
import {WidthService} from "../../../services/width.service";
import {FileService} from "../../../services/file.service";
import {Router} from "@angular/router";
import {FileEntity} from "../../../models/FileEntity";
import {environment} from "../../../../environments/environment";
import {GalleryService} from "../../../services/gallery.service";
import {MenuService} from "../../../services/menu.service";

@Component({
  selector: 'app-file-list-item-file',
  templateUrl: './file-list-item-file.component.html',
  styleUrls: ['./file-list-item-file.component.css']
})
export class FileListItemFileComponent implements OnInit {

  @Input() file:FileEntity;
  @Input() index:number;

  imageLoaded = false;

  ignoreNextClick = false;

  constructor(private menuService:MenuService,public widthService:WidthService,public fileService:FileService,private router:Router,private galleryService:GalleryService) { }

  ngOnInit(): void {
  }

  getSrc(){
    return `${environment.AUTHORIZATION_SERVER_URL}/api/file/getThumbnail?fileId=${this.file.id}`;
  }

  select(event:MouseEvent){


    if(this.ignoreNextClick){
      this.ignoreNextClick=false;
      return;
    }


    if(this.fileService.touchSelect){

      if(this.fileService.selectedFiles.includes(this.file)){
        this.fileService.selectedFiles.splice(this.fileService.selectedFiles.indexOf(this.file),1)
      }else {
        this.fileService.selectedFiles.push(this.file)
      }

    }else {

      if(event.ctrlKey){

        if(this.isSelected()){
          this.fileService.selectedFiles.splice(this.fileService.selectedFiles.indexOf(this.file),1)
          this.fileService.shiftSelectLastIndex = null;
        }else{
          this.fileService.selectedFiles.push(this.file);
          this.fileService.shiftSelectLastIndex = this.index;
        }

      }else if(event.shiftKey){

        this.fileService.shiftSelect(this.index)

      }else {
        this.fileService.selectedFolders = [];
        this.fileService.selectedFiles = [this.file];
        this.fileService.shiftSelectLastIndex = this.index;
      }

    }

    this.fileService.disableTouchSelectIfEmpty()
  }

  isSelected(){
    return this.fileService.selectedFiles.includes(this.file);
  }

  hold(event:any){

    if(event.pointerType!="touch"){
      return;
    }




    this.ignoreNextClick = true;

    if(!this.fileService.touchSelect){
      this.fileService.touchSelect = true;
      this.fileService.selectedFiles = []
      this.fileService.selectedFolders = []
    }

    if(this.fileService.selectedFiles.includes(this.file)){
      this.fileService.selectedFiles.splice(this.fileService.selectedFiles.indexOf(this.file),1)
    }else {
      this.fileService.selectedFiles.push(this.file)
    }

    this.fileService.disableTouchSelectIfEmpty()

  }


  tapped(event: any) {


    if(!this.file.hasThumbnail){
      return;
    }

    if(this.fileService.touchSelect){
      return;
    }

    if (event.tapCount == 2) {

      this.galleryService.loadPhotos(this.file)
      this.galleryService.displayGallery = true;



    }

  }

  contextMenu(event:MouseEvent){

    event.preventDefault()

    if(!this.fileService.selectedFiles.includes(this.file)){
      this.fileService.clearSelection()
      this.fileService.selectedFiles.push(this.file)
    }


    if(document.body.clientWidth-event.clientX<300){
      this.menuService.left=event.clientX-250
    }else{
      this.menuService.left=event.clientX
    }

    this.menuService.moveToTop = document.body.clientHeight-event.clientY<200



    this.menuService.top=event.clientY
    this.menuService.displayMenu=true
  }


}
