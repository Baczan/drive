import {Component, Input, OnInit} from '@angular/core';
import {Folder} from "../../../models/Folder";
import {WidthService} from "../../../services/width.service";
import {FileService} from "../../../services/file.service";
import {Router} from "@angular/router";
import {FileEntity} from "../../../models/FileEntity";
import {environment} from "../../../../environments/environment";

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

  constructor(public widthService:WidthService,public fileService:FileService,private router:Router) { }

  ngOnInit(): void {
  }

  getSrc(){
    return `${environment.AUTHORIZATION_SERVER_URL}/api/file/getThumbnail?fileId=${this.file.id}`;
  }

  select(event:any){

    if(this.ignoreNextClick){
      this.ignoreNextClick=false;
      return;
    }

    console.log(1)

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

}
