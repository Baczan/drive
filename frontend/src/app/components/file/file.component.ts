import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {FileService} from "../../services/file.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormControl, Validators} from "@angular/forms";
import {finalize} from "rxjs/operators";
import {HttpErrorResponse} from "@angular/common/http";
import {ActivatedRoute, Route, Router} from "@angular/router";
import {MatBottomSheet} from "@angular/material/bottom-sheet";
import {FileTransferSheetComponent} from "./file-transfer-sheet/file-transfer-sheet.component";

@Component({
  selector: 'app-file',
  templateUrl: './file.component.html',
  styleUrls: ['./file.component.css']
})
export class FileComponent implements OnInit, AfterViewInit {

  @ViewChild("listElement") list: ElementRef;
  @ViewChild("fileInput") fileInput: HTMLInputElement;


  constructor(public fileService: FileService, public dialog: MatDialog, private cdr: ChangeDetectorRef, private route: ActivatedRoute, private router: Router,private bottomSheet: MatBottomSheet) {
  }

  ngOnInit(): void {

    this.route.params.subscribe(params => {


      this.fileService.currentFolderId = params.id;

      this.fileService.getFilesAndFolders().subscribe(response => {

      }, (error: HttpErrorResponse) => {

        if (error.error == "illegal_argument") {
          this.router.navigate(["files"])
        }

      })

    });


  }


  ngAfterViewInit(): void {
  }


  fileInputClick(event: any) {

    console.log(event)

    // @ts-ignore
    let fileList: FileList = event.target.files;

    for (let i = 0; i < fileList.length; i++) {
      // @ts-ignore
      this.fileService.uploadFile(fileList.item(i));
    }

    //event.dataTransfer.items.clear()

    this.fileInput.value = "";

  }

  createFolder() {


    const dialogRef = this.dialog.open(FolderCreationDialogComponent, {
      width: "400px",
    });


  }

  displayDownload(){
    return this.fileService.selectedFolders.length>0 || this.fileService.selectedFiles.length>0;
  }

  openBottomSheet(){
    this.bottomSheet.open(FileTransferSheetComponent,{panelClass:"sheet"})
  }

  badgeNumber():number{
    return this.fileService.uploading.filter(fileUploading=>!fileUploading.seen).length + this.fileService.currentZipFiles.filter(zipFile=>!zipFile.seen).length;
  }

  fileDropped(files:any){

  }
}

@Component({
  selector: 'folder-creation-dialog',
  templateUrl: 'folder-creation-dialog.html',
  styleUrls: ['./folder-creation-dialog.component.css']
})
export class FolderCreationDialogComponent {


  folderNameControl: FormControl = new FormControl("", Validators.required);
  creatingFolder: boolean = false;

  constructor(
    public dialogRef: MatDialogRef<FolderCreationDialogComponent>, private fileService: FileService) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }



  createFolder() {

    this.folderNameControl.markAllAsTouched();

    if (this.folderNameControl.invalid) {
      return;
    }

    let index = this.fileService.folders.findIndex(folder => {
      return folder.folderName == this.folderNameControl.value;
    });


    if (index >= 0) {
      this.folderNameControl.setErrors({notUnique: "notUnique"});
      return;
    }


    this.creatingFolder = true;

    this.fileService.createFolder(this.folderNameControl.value)
      .pipe(finalize(() => {
        this.creatingFolder = false;
      }))
      .subscribe(response => {


        this.dialogRef.close();

      }, (error: HttpErrorResponse) => {

        if (error.error == "not_unique") {
          this.folderNameControl.setErrors({notUnique: "notUnique"})
        }
        console.log(error)
      })
  }





}

