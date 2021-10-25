import {Injectable} from '@angular/core';
import {FileService} from "./file.service";
import {FileEntity} from "../models/FileEntity";

@Injectable({
  providedIn: 'root'
})
export class GalleryService {

  displayGallery: boolean = false;

  photos: FileEntity[] = []
  currentIndex = 0

  constructor(private fileService: FileService) {
  }

  loadPhotos(fileEntity:FileEntity) {
    this.photos = this.fileService.files.filter(file => file.hasThumbnail)
    this.currentIndex = this.photos.indexOf(fileEntity);

  }

}
