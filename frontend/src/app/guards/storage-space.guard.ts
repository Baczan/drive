import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import {FileService} from "../services/file.service";

@Injectable({
  providedIn: 'root'
})
export class StorageSpaceGuard implements CanActivate {

  constructor(private fileService:FileService) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    this.fileService.getStorageSpace()
    this.fileService.getFavoriteFolder()
    return true;
  }

}
