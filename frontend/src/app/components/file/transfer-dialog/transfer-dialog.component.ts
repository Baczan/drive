import {Component, OnInit} from '@angular/core';
import {TransferFolder} from "../../../models/TransferFolder";
import {FileService} from "../../../services/file.service";
import {Folder} from "../../../models/Folder";
import {FileEntity} from "../../../models/FileEntity";
import {MatDialogRef} from "@angular/material/dialog";
import {finalize} from "rxjs/operators";

@Component({
  selector: 'app-transfer-dialog',
  templateUrl: './transfer-dialog.component.html',
  styleUrls: ['./transfer-dialog.component.css']
})
export class TransferDialogComponent implements OnInit {

  // @ts-ignore
  currentFolder: Folder = null;

  folderTransferOptions: TransferFolder[] = [];

  files: FileEntity[] = []

  constructor(public dialogRef: MatDialogRef<TransferDialogComponent>, public fileService: FileService) {
  }

  ngOnInit(): void {

    // @ts-ignore
    this.changeFolder(null)

  }


  changeFolder(folderId: string) {


    this.fileService.getTransferOptions(folderId)
      .subscribe(response => {
        this.folderTransferOptions = response.transferFolders.sort((a, b) => (a.folder.folderName > b.folder.folderName) ? 1 : -1);
        this.currentFolder = response.folder;
        this.files = response.files.sort((a, b) => (a.filename.toLowerCase() > b.filename.toLowerCase()) ? 1 : -1);
      }, error => {
        this.dialogRef.close();
      })


  }

  goBack() {

    if (!this.currentFolder) {
      return;
    }

    // @ts-ignore
    this.changeFolder(this.currentFolder.parentId)

  }

  canTransfer(): boolean {

    if (!this.currentFolder && !this.fileService.currentFolder) {
      return false;
    } else if (this.currentFolder && this.fileService.currentFolder) {
      return this.currentFolder.id != this.fileService.currentFolder.id
    } else {
      return true
    }

  }

  transfer() {

    let folderId = null;

    if (this.currentFolder) {
      folderId = this.currentFolder.id;
    }
    this.fileService.transfer(folderId).subscribe(response => {
      this.fileService.getFilesAndFolders().pipe(finalize(()=>{
        this.dialogRef.close()
      })).subscribe()
    },error => {
      this.dialogRef.close();
    })
  }


}
