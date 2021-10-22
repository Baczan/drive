import {FileEntity} from "./FileEntity";
import {Folder} from "./Folder";

export class FilesAndFoldersDTO {
  public files:FileEntity[];
  public folders:Folder[];
  public parentFolder:Folder;
}
