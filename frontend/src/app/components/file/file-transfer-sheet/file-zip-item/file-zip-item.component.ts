import {ChangeDetectorRef, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ZipInfo} from "../../../../models/ZipInfo";
import {FileService} from "../../../../services/file.service";
import {MatBottomSheetRef} from "@angular/material/bottom-sheet";
import {FileTransferSheetComponent} from "../file-transfer-sheet.component";

@Component({
  selector: 'app-file-zip-item',
  templateUrl: './file-zip-item.component.html',
  styleUrls: ['./file-zip-item.component.css']
})
export class FileZipItemComponent implements OnInit {

  @Input() zipInfo:ZipInfo;

  constructor(private fileService:FileService,public sheetRef: MatBottomSheetRef<FileTransferSheetComponent>,private cdr:ChangeDetectorRef) { }

  ngOnInit(): void {
    this.zipInfo.seen = true;
  }

  cancelDownloading(){
    this.fileService.currentZipFiles.splice(this.fileService.currentZipFiles.indexOf(this.zipInfo), 1);
  }

}
