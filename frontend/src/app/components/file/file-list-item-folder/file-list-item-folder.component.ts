import {Component, Input, OnInit} from '@angular/core';
import {WidthService} from "../../../services/width.service";
import {Folder} from "../../../models/Folder";
import {FileService} from "../../../services/file.service";
import {Router} from "@angular/router";
import {MenuService} from "../../../services/menu.service";

@Component({
  selector: 'app-file-list-item-folder',
  templateUrl: './file-list-item-folder.component.html',
  styleUrls: ['./file-list-item-folder.component.css']
})
export class FileListItemFolderComponent implements OnInit {

  @Input() folder: Folder;
  @Input() returnFolder: boolean = false;
  @Input() index: number;

  ignoreNextClick: boolean = false;

  constructor(private menuService:MenuService,public widthService: WidthService, public fileService: FileService, private router: Router) {
  }

  ngOnInit(): void {
  }

  tapped(event: any) {

    if(this.fileService.touchSelect && !this.returnFolder){
      return;
    }

    if (event.tapCount == 2) {

      if (this.returnFolder) {
        if (this.fileService.currentFolder.parentId) {
          this.router.navigate(["files", this.fileService.currentFolder.parentId]);
        } else {
          this.router.navigate(["files"]);
        }

      } else {
        this.router.navigate(["files", this.folder.id]);
      }


    }

  }

  select(event: any) {

    if (this.returnFolder) {
      return;
    }

    if(this.ignoreNextClick){
      this.ignoreNextClick=false;
      return;
    }

    if (this.fileService.touchSelect) {

      if (this.fileService.selectedFolders.includes(this.folder)) {
        this.fileService.selectedFolders.splice(this.fileService.selectedFolders.indexOf(this.folder), 1)
      } else {
        this.fileService.selectedFolders.push(this.folder)
      }

    } else {

      if (event.ctrlKey) {

        if (this.isSelected()) {
          this.fileService.selectedFolders.splice(this.fileService.selectedFolders.indexOf(this.folder), 1)
          this.fileService.shiftSelectLastIndex = null;
        } else {
          this.fileService.selectedFolders.push(this.folder);
          this.fileService.shiftSelectLastIndex = this.index;
        }

      } else if (event.shiftKey) {

        this.fileService.shiftSelect(this.index)

      } else {
        this.fileService.selectedFiles = [];
        this.fileService.selectedFolders = [this.folder];
        this.fileService.shiftSelectLastIndex = this.index;
      }

    }

    this.fileService.disableTouchSelectIfEmpty()
  }

  isSelected() {
    return this.fileService.selectedFolders.includes(this.folder);
  }

  hold(event: any) {

    if (this.returnFolder) {
      return;
    }

    if (event.pointerType != "touch") {
      return;
    }


    this.ignoreNextClick = true;

    if (!this.fileService.touchSelect) {
      this.fileService.touchSelect = true;
      this.fileService.selectedFiles = []
      this.fileService.selectedFolders = []
    }

    if (this.fileService.selectedFolders.includes(this.folder)) {
      this.fileService.selectedFolders.splice(this.fileService.selectedFolders.indexOf(this.folder), 1)
    } else {
      this.fileService.selectedFolders.push(this.folder)
    }

    this.fileService.disableTouchSelectIfEmpty()

  }


  contextMenu(event:MouseEvent){

    if(this.returnFolder){
      return;
    }

    event.preventDefault()

    if(!this.fileService.selectedFolders.includes(this.folder)){
      this.fileService.clearSelection()
      this.fileService.selectedFolders.push(this.folder)
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
