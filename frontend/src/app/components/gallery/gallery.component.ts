import {Component, OnInit} from '@angular/core';
import {GalleryService} from "../../services/gallery.service";
import {FileEntity} from "../../models/FileEntity";
import {environment} from "../../../environments/environment";

@Component({
  selector: 'app-gallery',
  templateUrl: './gallery.component.html',
  styleUrls: ['./gallery.component.css']
})
export class GalleryComponent implements OnInit {

  constructor(public galleryService:GalleryService) {
  }

  ngOnInit(): void {
  }

  getPhotoUrl(fileEntity:FileEntity){

    return `${environment.AUTHORIZATION_SERVER_URL}/api/file/download?fileId=${fileEntity.id}&displayPhoto=${true}`;
  }

  nextPhoto(event:MouseEvent){
    event.stopPropagation()
    this.galleryService.currentIndex+=1;
  }

  previousPhoto(event:MouseEvent){
    event.stopPropagation()
    this.galleryService.currentIndex-=1;
  }

  nextPhotoSwipe(){

    if(this.galleryService.currentIndex+1<this.galleryService.photos.length){
      this.galleryService.currentIndex+=1;
    }

  }

  previousPhotoSwipe(){
    if(this.galleryService.currentIndex>0){
      this.galleryService.currentIndex-=1;
    }
  }

  close(event:any){

    if(event instanceof PointerEvent){
      if(event.pointerType!="touch"){
        this.galleryService.displayGallery = false;
      }
    }

  }

  forceClose(){
    this.galleryService.displayGallery = false;
  }

}
