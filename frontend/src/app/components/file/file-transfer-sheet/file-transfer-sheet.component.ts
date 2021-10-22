import { Component, OnInit } from '@angular/core';
import {FileService} from "../../../services/file.service";
import {MatBottomSheetRef} from "@angular/material/bottom-sheet";

@Component({
  selector: 'app-file-transfer-sheet',
  templateUrl: './file-transfer-sheet.component.html',
  styleUrls: ['./file-transfer-sheet.component.css']
})
export class FileTransferSheetComponent implements OnInit {

  constructor(public fileService:FileService,public sheetRef:MatBottomSheetRef<FileTransferSheetComponent>) { }

  ngOnInit(): void {

    this.fileService.zipDeleted.subscribe(()=>{
      if (this.fileService.uploading.length == 0 && this.fileService.currentZipFiles.length==0) {
        this.sheetRef.dismiss()
      }
    })

  }

}
