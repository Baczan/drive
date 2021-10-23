import {Directive, EventEmitter, HostBinding, HostListener, Output} from '@angular/core';
import {FileService} from "../services/file.service";

@Directive({
  selector: '[appDropzone]'
})
export class DropzoneDirective {

  constructor(private fileService:FileService) {
  }

  @HostBinding('class.fileover') fileOver: boolean;
  @Output() fileDropped = new EventEmitter<any>();

  // Dragover listener
  @HostListener('dragover', ['$event']) onDragOver(evt:DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.fileOver = true;
    this.fileService.dragging = true;
  }

  // Dragleave listener
  @HostListener('dragleave', ['$event']) public onDragLeave(evt:DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.fileOver = false;
    this.fileService.dragging = false;
  }

  // Drop listener
  @HostListener('drop', ['$event']) public ondrop(evt:DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.fileOver = false;
    this.fileService.dragging = false;
    // @ts-ignore
    let files = evt.dataTransfer.files;


    // @ts-ignore
    this.getFilesWebkitDataTransferItems(evt.dataTransfer.items).then((files:File[])=>{
      files.forEach(file=>{
        this.fileService.uploadFile(file)
      })
    })

  }

  //Stack overflow: https://stackoverflow.com/questions/3590058/does-html5-allow-drag-drop-upload-of-folders-or-a-folder-tree/53058574
  getFilesWebkitDataTransferItems(dataTransferItems:DataTransferItemList) {
    function traverseFileTreePromise(item:any, path='') {
      return new Promise( resolve => {
        if (item.isFile) {
          item.file((file:any) => {
            file.filepath = path + file.name //save full path
            files.push(file)
            resolve(file)
          })
        } else if (item.isDirectory) {
          let dirReader = item.createReader()
          dirReader.readEntries((entries:any) => {
            let entriesPromises = []
            for (let entr of entries)
              entriesPromises.push(traverseFileTreePromise(entr, path + item.name + "/"))
            resolve(Promise.all(entriesPromises))
          })
        }
      })
    }

    let files:any = []
    return new Promise((resolve, reject) => {
      let entriesPromises = []
      // @ts-ignore
      for (let it of dataTransferItems)
        entriesPromises.push(traverseFileTreePromise(it.webkitGetAsEntry()))
      Promise.all(entriesPromises)
        .then(entries => {
          resolve(files)
        })
    })
  }

}
