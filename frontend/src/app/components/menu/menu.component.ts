import {Component, HostListener, OnInit} from '@angular/core';
import {FileService} from "../../services/file.service";
import {FolderNameChangeComponent} from "../file/dialogs/folder-name-change/folder-name-change.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {

  constructor(public fileService:FileService,private dialog:MatDialog) { }

  ngOnInit(): void {
  }

  changeFolderName(){
    const dialogRef = this.dialog.open(FolderNameChangeComponent, {
      width: "400px",
      autoFocus: false
    });
  }

  starIconMode(){
    let value = false;

    this.fileService.selectedFolders.forEach(folder=>{
      if(!folder.favorite){
        value = true;
      }
    })

    return value;
  }

}
