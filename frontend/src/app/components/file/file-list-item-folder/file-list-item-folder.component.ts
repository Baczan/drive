import {Component, Input, OnInit} from '@angular/core';
import {WidthService} from "../../../services/width.service";
import {Folder} from "../../../models/Folder";
import {FileService} from "../../../services/file.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-file-list-item-folder',
  templateUrl: './file-list-item-folder.component.html',
  styleUrls: ['./file-list-item-folder.component.css']
})
export class FileListItemFolderComponent implements OnInit {

  @Input() folder:Folder;
  @Input() returnFolder:boolean = false;
  @Input() index:number;

  constructor(public widthService:WidthService,public fileService:FileService,private router:Router) { }

  ngOnInit(): void {
  }

  tapped(event:any){

    if(event.tapCount==2){

      if(this.returnFolder){
        if(this.fileService.currentFolder.parentId){
          this.router.navigate(["files",this.fileService.currentFolder.parentId]);
        }else {
          this.router.navigate(["files"]);
        }

      }else{
        this.router.navigate(["files",this.folder.id]);
      }


    }

  }

  select(event:any){

    if(this.returnFolder){
      return;
    }

    if(event.ctrlKey){

      if(this.isSelected()){
        this.fileService.selectedFolders.splice(this.fileService.selectedFolders.indexOf(this.folder),1)
        this.fileService.shiftSelectLastIndex = null;
      }else{
        this.fileService.selectedFolders.push(this.folder);
        this.fileService.shiftSelectLastIndex = this.index;
      }

    }else if(event.shiftKey){

      this.fileService.shiftSelect(this.index)

    }else {
      this.fileService.selectedFiles = [];
      this.fileService.selectedFolders = [this.folder];
      this.fileService.shiftSelectLastIndex = this.index;
    }

  }

  isSelected(){
    return this.fileService.selectedFolders.includes(this.folder);
  }

  dragStart(event:DragEvent){
    console.log(event)
  }

  drop(event:DragEvent){
    event.stopPropagation()
    console.log(event)
  }

}
