import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {FileUploading} from "../../../../models/FileUploading";
import {FileService} from "../../../../services/file.service";
import {MatBottomSheetRef} from "@angular/material/bottom-sheet";
import {FileTransferSheetComponent} from "../file-transfer-sheet.component";

@Component({
  selector: 'app-file-uploading-item',
  templateUrl: './file-uploading-item.component.html',
  styleUrls: ['./file-uploading-item.component.css']
})
export class FileUploadingItemComponent implements OnInit, OnDestroy {

  @Input() fileUploading: FileUploading;

  constructor(private fileService: FileService, public sheetRef: MatBottomSheetRef<FileTransferSheetComponent>) {

  }

  ngOnInit(): void {
    this.fileUploading.seen = true;
  }

  cancelUploading() {
    this.fileUploading.subscription.unsubscribe();
    this.fileService.uploading.splice(this.fileService.uploading.indexOf(this.fileUploading), 1);
    this.closeSheetIfEmpty()
  }

  ngOnDestroy(): void {
    this.closeSheetIfEmpty()
  }

  closeSheetIfEmpty() {
    if (this.fileService.uploading.length == 0 && this.fileService.currentZipFiles.length==0) {
      this.sheetRef.dismiss()
    }
  }

}
