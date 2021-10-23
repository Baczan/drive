import {Component, OnInit} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";
import {MatDialogRef} from "@angular/material/dialog";
import {FileService} from "../../../../services/file.service";
import {finalize} from "rxjs/operators";

@Component({
  selector: 'app-folder-name-change',
  templateUrl: './folder-name-change.component.html',
  styleUrls: ['./folder-name-change.component.css']
})
export class FolderNameChangeComponent implements OnInit {

  folderNameControl: FormControl = new FormControl(this.fileService.selectedFolders[0].folderName, Validators.required);
  changingName: boolean = false;

  constructor(public dialogRef: MatDialogRef<FolderNameChangeComponent>, public fileService: FileService) {
  }

  ngOnInit(): void {
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  changeName() {

    this.folderNameControl.markAllAsTouched();

    if (this.folderNameControl.invalid) {
      return;
    }

    if (this.folderNameControl.value == this.fileService.selectedFolders[0].folderName) {
      this.dialogRef.close()
      return;
    }


    let index = this.fileService.folders.findIndex(folder => {
      return folder.folderName == this.folderNameControl.value;
    });


    if (index >= 0) {
      this.folderNameControl.setErrors({notUnique: "notUnique"});
      return;
    }

    this.changingName = true;

    this.fileService.changeFolderName(this.folderNameControl.value)
      .pipe(
        finalize(() => {
            this.changingName = false;
          }
        ))
      .subscribe(response => {

        this.fileService.selectedFolders = []
        let index = this.fileService.folders.findIndex(folder1 => folder1.id == response.id)
        if (index >= 0) {
          this.fileService.folders[index] = response;
        }

        this.fileService.getFavoriteFolder();

        this.closeDialog();

      }, error => {

        if (error.error == "not_unique") {
          this.folderNameControl.setErrors({notUnique: "notUnique"})
        } else {
          this.fileService.getFilesAndFolders();
          this.closeDialog();
        }

      })

  }

}
