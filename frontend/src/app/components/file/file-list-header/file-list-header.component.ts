import {ChangeDetectorRef, Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {WidthService} from "../../../services/width.service";
import {Width} from "../../../models/Width";
import {FileService} from "../../../services/file.service";

@Component({
  selector: 'app-file-list-header',
  templateUrl: './file-list-header.component.html',
  styleUrls: ['./file-list-header.component.css']
})
export class FileListHeaderComponent implements OnInit,OnChanges {


  constructor(public widthService:WidthService,private cdr:ChangeDetectorRef,public fileService:FileService) { }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {

  }

  changeSorting(value:string){


    if(this.fileService.currentSort==value){

      this.fileService.sortDirection = this.fileService.sortDirection=='asc'?'desc':'asc';

    }else {

      this.fileService.currentSort=value;
      this.fileService.sortDirection = "asc";

    }

    this.fileService.sort();

  }


}
