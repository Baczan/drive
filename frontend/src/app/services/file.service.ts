import {EventEmitter, Inject, Injectable} from '@angular/core';
import {FileUploading} from "../models/FileUploading";
import {environment} from "../../environments/environment";
import {HttpClient, HttpEventType} from "@angular/common/http";
import {FilesAndFoldersDTO} from "../models/FilesAndFoldersDTO";
import {finalize, tap} from "rxjs/operators";
import {Folder} from "../models/Folder";
import {FileEntity} from "../models/FileEntity";
import {webSocket} from "rxjs/webSocket";
import {WebsocketService} from "./websocket.service";
import {DOCUMENT} from "@angular/common";
import {ZipInfo} from "../models/ZipInfo";
import {MatBottomSheetRef} from "@angular/material/bottom-sheet";
import {FileTransferSheetComponent} from "../components/file/file-transfer-sheet/file-transfer-sheet.component";
import {StorageSpace} from "../models/StorageSpace";
import {MatSnackBar} from "@angular/material/snack-bar";
import {TransferFolder} from "../models/TransferFolder";
import {TransferFolderResponse} from "../models/TransferFolderResponse";
import {TransferRequestBody} from "../models/TransferRequestBody";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  uploading: FileUploading[] = [];

  // @ts-ignore
  currentFolderId: string = null;
  //@ts-ignore
  currentFolder: Folder = null;
  folders: Folder[] = [];
  files: FileEntity[] = [];

  gettingFiles: boolean = false;

  selectedFiles: FileEntity[] = [];
  selectedFolders: Folder[] = [];

  shiftSelectLastIndex: any = null;

  currentSort = "name";
  sortDirection = "asc";

  currentZipFiles: ZipInfo[] = [];
  zipDeleted: EventEmitter<boolean> = new EventEmitter<boolean>();

  dragging: boolean = false;

  storageSpace: StorageSpace;

  favoriteFolders: Folder[] = []

  touchSelect: boolean = false;


  constructor(private snackbar: MatSnackBar, private http: HttpClient, private websocket: WebsocketService, @Inject(DOCUMENT) private document: HTMLDocument) {

    websocket.rxStomp.watch("/user/queue/zip").subscribe(response => {

      //Wait for initial response to be added to list
      setTimeout(() => {

        let zipInfo: ZipInfo = JSON.parse(response.body);
        console.log(zipInfo)
        let index = this.currentZipFiles.findIndex(zipInfo1 => {
          return zipInfo1.id == zipInfo.id
        });

        if (index >= 0) {

          this.currentZipFiles[index].error = zipInfo.error;
          this.currentZipFiles[index].progress = zipInfo.progress;

          if (zipInfo.completed) {


            const url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/downloadZip?zipId=${zipInfo.id}`;

            let href = this.document.createElement("a");
            href.download = "true";
            href.href = url;
            href.click();

            this.currentZipFiles.splice(index, 1);
            this.zipDeleted.emit(true);
          }

        }

      }, 1000)


    })

    websocket.rxStomp.watch("/user/queue/storageSpace").subscribe(response => {

      this.storageSpace = JSON.parse(response.body);
    })

  }

  uploadFile(file: File) {

    let fileUploading: FileUploading = new FileUploading();
    fileUploading.filename = file.name;

    let url;
    if (this.currentFolderId) {
      url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/upload?folderId=${this.currentFolderId}`;
    } else {
      url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/upload`;
    }

    let formData = new FormData();
    formData.append("file", file, file.name);

    fileUploading.subscription = this.http.post<FileEntity>(url, formData, {reportProgress: true, observe: "events"})
      .subscribe(response => {

        if (response.type == HttpEventType.UploadProgress) {
          // @ts-ignore
          fileUploading.progress = (response.loaded / response.total);
        }

        if (response.type == HttpEventType.Response) {
          fileUploading.completed = true;

          // @ts-ignore
          if (this.currentFolderId == response.body.folderId) {
            // @ts-ignore
            this.files.push(response.body);
            this.clearSelection();
            this.sort();
          }

          this.getStorageSpace()

          this.uploading.splice(this.uploading.indexOf(fileUploading), 1)

        }

      }, error => {
        fileUploading.error = true;
      });

    this.uploading.push(fileUploading);
  }

  createFolder(folderName: string) {

    let url;

    if (this.currentFolderId) {
      url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/create?folderName=${folderName}&parentId=${this.currentFolderId}`;
    } else {
      url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/create?folderName=${folderName}`;
    }

    return this.http.post<Folder>(url, null).pipe(tap((response) => {
      this.folders.push(response);
      this.clearSelection();
      this.sort();
    }));

  }

  getFilesAndFolders() {

    let url;

    if (this.currentFolderId) {
      url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/getAll?folderId=${this.currentFolderId}`;
    } else {
      url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/getAll`;
    }

    this.gettingFiles = true;
    return this.http.get<FilesAndFoldersDTO>(url)
      .pipe(tap(response => {

          this.files = response.files;
          this.folders = response.folders;
          this.currentFolder = response.parentFolder;

          this.clearSelection();
          this.sort();
          this.touchSelect = false;

        })
        , finalize(() => {
          this.gettingFiles = false;
        }))

  }


  download() {
    if (this.selectedFiles.length == 1 && this.selectedFolders.length == 0) {

      let href = this.document.createElement("a");
      href.download = "true";
      href.href = `${environment.AUTHORIZATION_SERVER_URL}/api/file/download?fileId=${this.selectedFiles[0].id}`
      href.click();

    } else {

      let filesId = "";

      for (let i = 0; i < this.selectedFiles.length; i++) {

        if (i != 0) {
          filesId += ","
        }
        filesId += this.selectedFiles[i].id

      }

      let foldersId = "";

      for (let i = 0; i < this.selectedFolders.length; i++) {

        if (i != 0) {
          foldersId += ","
        }
        foldersId += this.selectedFolders[i].id

      }

      let url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/downloadMultiple?filesId=${filesId}&foldersId=${foldersId}`;

      if (this.currentFolderId != null) {
        url += `&parentId=${this.currentFolderId}`
      }

      this.http.get<ZipInfo>(url).subscribe(response => {

        this.currentZipFiles.push(response);
      })

    }

    this.clearSelection()
  }

  shiftSelect(index: number) {


    if (this.shiftSelectLastIndex != null) {

      if (index > this.shiftSelectLastIndex) {

        for (let i = this.shiftSelectLastIndex + 1; i <= index; i++) {
          this.selectByIndex(i)
        }

      } else if (index < this.shiftSelectLastIndex) {

        for (let i = this.shiftSelectLastIndex; i >= index; i--) {
          this.selectByIndex(i)
        }

      }

    }
    this.shiftSelectLastIndex = index;
    this.selectByIndex(index);

  }

  selectByIndex(index: number) {

    if (index < this.folders.length) {

      let folder: Folder = this.folders[index];

      if (!this.selectedFolders.includes(folder)) {
        this.selectedFolders.push(folder);
      }

    } else {

      let file: FileEntity = this.files[index - this.folders.length];

      if (!this.selectedFiles.includes(file)) {
        this.selectedFiles.push(file)
      }
    }

  }

  clearSelection() {
    this.shiftSelectLastIndex = null;
    this.selectedFiles = [];
    this.selectedFolders = [];
    this.touchSelect = false
  }


  sort() {

    if (this.currentSort == "name") {

      if (this.sortDirection == "asc") {
        this.sortByName()
      } else {
        this.sortByNameDesc()
      }

    } else if (this.currentSort == "size") {

      if (this.sortDirection == "asc") {
        this.sortBySize()
      } else {
        this.sortBySizeDesc()
      }

    } else if (this.currentSort == "date") {
      if (this.sortDirection == "asc") {
        this.sortByDate()
      } else {
        this.sortByDateDesc()
      }
    }

  }


  sortByName() {
    this.files.sort((a, b) => (a.filename.toLowerCase() > b.filename.toLowerCase()) ? 1 : -1);
    this.folders.sort((a, b) => (a.folderName.toLowerCase() > b.folderName.toLowerCase()) ? 1 : -1);
  }

  sortByNameDesc() {
    this.files.sort((a, b) => (a.filename.toLowerCase() < b.filename.toLowerCase()) ? 1 : -1);
    this.folders.sort((a, b) => (a.folderName.toLowerCase() < b.folderName.toLowerCase()) ? 1 : -1);
  }

  sortBySize() {
    this.files.sort((a, b) => (a.size < b.size) ? 1 : -1);
  }

  sortBySizeDesc() {
    this.files.sort((a, b) => (a.size > b.size) ? 1 : -1);
  }

  sortByDate() {
    this.files.sort((a, b) => (a.date < b.date) ? 1 : -1);
  }

  sortByDateDesc() {
    this.files.sort((a, b) => (a.date > b.date) ? 1 : -1);
  }


  delete() {

    this.selectedFiles.forEach(file => {
      this.deleteFile(file)
    })

    this.selectedFiles = [];

    this.selectedFolders.forEach(folder => {
      this.deleteFolder(folder)
    })

    this.selectedFolders = [];

    this.clearSelection()

  }

  deleteFile(fileEntity: FileEntity) {

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/deleteFile?fileId=${fileEntity.id}`

    this.http.delete(url, {responseType: "text"}).subscribe(response => {

      this.getStorageSpace()
      this.files.splice(this.files.indexOf(fileEntity), 1);
    }, error => {

      //add message
      console.log(error)
    })

  }

  deleteFolder(folder: Folder) {

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/deleteFolder?folderId=${folder.id}`

    this.http.delete(url, {responseType: "text"}).subscribe(response => {

      this.getStorageSpace()
      this.folders.splice(this.folders.indexOf(folder), 1);

      this.getFavoriteFolder()
    }, error => {
      //add message
      console.log(error)
    })

  }

  getStorageSpace() {

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/file/storageSpace`;

    this.http.get<StorageSpace>(url).subscribe(response => {
      this.storageSpace = response;
    }, error => {
      //add message
      console.log(error)
    })

  }

  changeFolderName(name: string) {

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/changeName?folderId=${this.selectedFolders[0].id}&newName=${name}`

    return this.http.post<Folder>(url, null);
  }

  setFavorite() {

    if (this.selectedFolders.length == 0) {
      return;
    }

    let value = false;

    this.selectedFolders.forEach(folder => {
      if (!folder.favorite) {
        value = true;
      }
    })

    let folderList = this.selectedFolders[0].id;

    for (let i = 1; i < this.selectedFolders.length; i++) {
      folderList += "," + this.selectedFolders[i].id
    }

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/setFavorite?folderIds=${folderList}&value=${value}`

    return this.http.post<Folder[]>(url, null).subscribe(response => {

      response.forEach(folder => {

        let index = this.folders.findIndex(folder1 => folder1.id == folder.id);

        if (index >= 0) {
          this.folders[index].favorite = folder.favorite;
        }

      })

      this.getFavoriteFolder();

      this.selectedFolders = [];
    }, error => {

      this.snackbar.open("Błąd podczas dodawania do ulubionych", "OK")

    });
  }

  getFavoriteFolder() {

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/getFavorites`

    this.http.get<Folder[]>(url).subscribe(response => {
      this.favoriteFolders = response;
    }, error => {
      //add message
      console.log(error)
    })

  }

  disableTouchSelectIfEmpty() {

    if (this.touchSelect && this.selectedFiles.length == 0 && this.selectedFolders.length == 0) {
      this.touchSelect = false;
    }

  }

  getTransferOptions(folderId: any) {

    let url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/transferOptions`;

    if (folderId) {
      url += "?folderId=" + folderId;
    }


    let folders: string[] = this.selectedFolders.map(folder => folder.id);

    return this.http.post<TransferFolderResponse>(url, folders);
  }


  transfer(folderId: any) {
    let url = `${environment.AUTHORIZATION_SERVER_URL}/api/folder/transfer`;

    if (folderId) {
      url += "?folderId=" + folderId;
    }

    let body: TransferRequestBody = new TransferRequestBody();

    body.folders = this.selectedFolders.map(folder => folder.id);
    body.files = this.selectedFiles.map(file => file.id);

    return this.http.post(url, body, {responseType: "text"});
  }

  transferIconShowing(): boolean {

    if (!this.currentFolder) {

      if (this.selectedFolders.length == this.folders.length) {
        return false;
      }

    }

    return this.selectedFolders.length > 0 || this.selectedFiles.length > 0;
  }
}

